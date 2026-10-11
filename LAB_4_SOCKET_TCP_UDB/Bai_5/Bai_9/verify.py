"""Hai receiver that tham gia multicast; mac dinh tu chon interface loopback."""
import os
from pathlib import Path
import queue
import socket
import subprocess
import sys
import threading
import time

settings = subprocess.run(['java', '-XshowSettings:properties', '-version'], capture_output=True).stderr.decode('utf-8', errors='replace')
home = next(row.split('=', 1)[1].strip() for row in settings.splitlines() if row.strip().startswith('java.home ='))
java = str(Path(home) / 'bin' / ('java.exe' if os.name == 'nt' else 'java'))
base = [java, '-Dstdout.encoding=UTF-8', '-Dstderr.encoding=UTF-8']
listing = subprocess.run(base + ['Bai_5.Bai_9.MulticastReceiver', '--list'], capture_output=True, timeout=10).stdout.decode('utf-8')
loopback = [row.split(';')[0].split('=', 1)[1] for row in listing.splitlines()
            if row.startswith('name=') and 'up=true' in row and 'loopback=true' in row]
interface = sys.argv[1] if len(sys.argv) > 1 else (loopback[0] if loopback else None)
if interface is None:
    raise SystemExit('No loopback interface found; pass a lab interface name explicitly.')
log = ['BÀI 9 — MULTICAST THÔNG BÁO', 'OS: ' + os.name, 'Selected interface: ' + interface,
       'Group: 239.255.0.1; TTL=1; multicast loopback enabled',
       'Method: 2 Java receiver processes; 1 Java sender; UTF-8; timeout 3000 ms.',
       '\nInterface inventory:\n' + listing]
processes = []


def launch(name, *args):
    process = subprocess.Popen(base + ['Bai_5.Bai_9.' + name, *map(str, args)],
                               stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                               encoding='utf-8', bufsize=1)
    processes.append(process)
    lines = queue.Queue()
    def collect():
        for row in process.stdout:
            lines.put(row.rstrip())
    threading.Thread(target=collect, daemon=True).start()
    return process, lines


def next_line(lines, label):
    row = lines.get(timeout=8)
    log.append(label + ': ' + row)
    return row


def await_join(lines, label):
    while True:
        row = next_line(lines, label)
        if row.startswith('JOINED '):
            return
        if 'Lỗi receiver' in row:
            raise AssertionError(row)


with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as probe:
    probe.bind(('127.0.0.1', 0))
    port = probe.getsockname()[1]
try:
    receivers = [launch('MulticastReceiver', interface, port, 3, 3000) for _ in range(2)]
    for i, (_, lines) in enumerate(receivers, 1):
        await_join(lines, 'R' + str(i))
    sender, sender_lines = launch('MulticastSender', interface, port)
    next_line(sender_lines, 'Sender')
    next_line(sender_lines, 'Sender')
    messages = ['Thông báo phòng lab', 'Multicast message 2', 'Kết thúc thử nghiệm']
    for message in messages:
        sender.stdin.write(message + '\n')
        sender.stdin.flush()
        assert next_line(sender_lines, 'Sender').startswith('SENT ')
        time.sleep(0.1)
    sender.stdin.write('QUIT\n')
    sender.stdin.flush()
    assert sender.wait(timeout=5) == 0
    for i, (receiver, lines) in enumerate(receivers, 1):
        for number, message in enumerate(messages, 1):
            row = next_line(lines, 'R' + str(i))
            assert row.startswith(f'RECEIVED {number} ') and row.endswith(': ' + message), row
        assert next_line(lines, 'R' + str(i)) == 'TOTAL 3'
        assert next_line(lines, 'R' + str(i)).startswith('LEFT ')
        assert receiver.wait(timeout=5) == 0
    log.append('PASS: 2 receivers each received all 3 messages and left group before socket close.')

    # Khong gui: ghi nhan timeout khong quy ket ngay cho firewall.
    idle, lines = launch('MulticastReceiver', interface, port, 1, 500)
    await_join(lines, 'Idle')
    assert next_line(lines, 'Idle').startswith('TIMEOUT:')
    assert next_line(lines, 'Idle') == 'TOTAL 0'
    assert next_line(lines, 'Idle').startswith('LEFT ')
    assert idle.wait(timeout=5) == 0
    log.append('PASS: no-sender timeout reported; group left on timeout exit.')
    log.append('No live Wi-Fi/VPN/firewall block was induced. A timeout alone does not prove its cause; lab troubleshooting is in README.md.')
except Exception as error:
    log.append('FAIL / environment limitation: ' + repr(error))
    log.append('Try a multicast-capable lab interface on the same LAN. See README.md; do not claim successful delivery from SENT alone.')
    raise
finally:
    for process in processes:
        if process.poll() is None:
            process.terminate()
            process.wait(timeout=5)
    Path(__file__).with_name('minh_chung.txt').write_text('\n'.join(log) + '\n', encoding='utf-8')
print('PASS: two multicast receivers, UTF-8, explicit interface, leaveGroup and timeout cleanup.')
