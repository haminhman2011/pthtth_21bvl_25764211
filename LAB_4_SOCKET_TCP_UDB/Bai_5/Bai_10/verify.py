"""Kiem tra hai server discovery multicast va TCP service tren loopback."""
import os
from pathlib import Path
import queue
import re
import socket
import subprocess
import sys
import threading
import time

settings = subprocess.run(['java', '-XshowSettings:properties', '-version'], capture_output=True).stderr.decode('utf-8', errors='replace')
home = next(row.split('=', 1)[1].strip() for row in settings.splitlines() if row.strip().startswith('java.home ='))
java = str(Path(home) / 'bin' / ('java.exe' if os.name == 'nt' else 'java'))
base = [java, '-Dstdout.encoding=UTF-8', '-Dstderr.encoding=UTF-8']
listing = subprocess.run(base + ['Bai_5.Bai_10.DiscoveryClient', '--list'], capture_output=True, timeout=10).stdout.decode('utf-8')
loopback = [row.split(';')[0].split('=', 1)[1] for row in listing.splitlines()
            if row.startswith('name=') and 'loopback=true' in row and 'up=true' in row]
interface = sys.argv[1] if len(sys.argv) > 1 else (loopback[0] if loopback else None)
if not interface:
    raise SystemExit('No loopback interface; specify a multicast-capable lab interface.')
log = ['BÀI 10 — UDP DISCOVERY + TCP SERVICE', 'Interface: ' + interface,
       'UDP group: 239.255.0.1; discovery window: 1200 ms; 3 queries per window.',
       'Two server processes, same UDP port, different TCP ports.']
processes = []


def free_port(kind):
    with socket.socket(socket.AF_INET, kind) as probe:
        probe.bind(('127.0.0.1', 0))
        return probe.getsockname()[1]


def server(port, name, udp):
    process = subprocess.Popen(base + ['Bai_5.Bai_10.DiscoveryServer', interface, str(port), name, '1.0', str(udp)],
                               stdout=subprocess.PIPE, stderr=subprocess.STDOUT, encoding='utf-8')
    processes.append(process)
    lines = queue.Queue()
    def collect():
        for line in process.stdout:
            lines.put(line.rstrip())
    threading.Thread(target=collect, daemon=True).start()
    line = lines.get(timeout=8)
    log.append(line)
    assert line.startswith('READY SERVICE '), line
    return process


def client(udp, commands):
    start = time.monotonic()
    result = subprocess.run(base + ['Bai_5.Bai_10.DiscoveryClient', interface, '1200', str(udp)],
                            input=commands, capture_output=True, encoding='utf-8', timeout=10)
    elapsed = time.monotonic() - start
    assert result.returncode == 0 and not result.stderr, result.stderr
    log.append('\nClient session:\n' + result.stdout)
    log.append(f'Wall time including JVM startup: {elapsed:.3f} s')
    assert 1.1 <= elapsed < 8, elapsed
    return result.stdout


try:
    udp = free_port(socket.SOCK_DGRAM)
    first_port = free_port(socket.SOCK_STREAM)
    first = server(first_port, 'LabA', udp)
    second_port = free_port(socket.SOCK_STREAM)
    second = server(second_port, 'LabB', udp)
    for choice in ['1', '2']:
        output = client(udp, choice + '\nECHO Xin chào Việt Nam\nTIME\nQUIT\n')
        assert 'FOUND 2 service(s)' in output, output
        assert f':{first_port} version=1.0' in output and f':{second_port} version=1.0' in output
        assert int(re.search(r'duplicates=(\d+)', output).group(1)) >= 2, output
        selected = re.search(r'^' + choice + r'\. (\S+) (\S+):(\d+) version=1.0$', output, re.MULTILINE)
        assert selected, output
        name, source, port = selected.groups()
        assert f'TCP_CONNECTED {source}:{port}' in output, output
        assert f'Server: WELCOME {name} 1.0' in output, output
        assert 'Server: OK Xin chào Việt Nam' in output and 'Server: BYE' in output
        assert re.search(r'Server: OK \d{4}-\d{2}-\d{2}T', output), output
    log.append('PASS: two servers displayed once each; duplicates removed; both TCP services selectable; source IP used; UTF-8 ECHO/TIME/QUIT.')

    # Khong server: khong treo va khong ket noi TCP den dia chi mac dinh.
    first.terminate()
    first.wait(timeout=5)
    second.terminate()
    second.wait(timeout=5)
    output = client(udp, '')
    assert 'FOUND 0 service(s)' in output and 'TCP_CONNECTED' not in output
    log.append('PASS: no-server timeout ends discovery with zero services.')

    # Gui truc tiep phan hoi UDP sai den cong nguon cua DISCOVER_SERVICE.
    # Dung loopback cho phep thu rieng nay, khong can sua server Java.
    if interface in loopback:
        responder = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
        responder.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        responder.bind(('', udp))
        membership = socket.inet_aton('239.255.0.1') + socket.inet_aton('127.0.0.1')
        responder.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, membership)
        responder.settimeout(0.2)
        stopping = threading.Event()
        def bad_responses():
            while not stopping.is_set():
                try:
                    data, address = responder.recvfrom(2048)
                    if data == b'DISCOVER_SERVICE':
                        for bad in [b'WRONG', b'SERVICE Bad 0 1.0', b'SERVICE Bad abc 1.0', b'SERVICE Bad 6010', b'\xff']:
                            responder.sendto(bad, address)
                except socket.timeout:
                    continue
        reader = threading.Thread(target=bad_responses, daemon=True)
        reader.start()
        try:
            output = client(udp, '')
            assert 'FOUND 0 service(s)' in output
            assert int(re.search(r'invalid=(\d+)', output).group(1)) > 0
            log.append('PASS: malformed, invalid-port and invalid-UTF-8 replies rejected.')
        finally:
            stopping.set()
            reader.join(timeout=2)
            responder.setsockopt(socket.IPPROTO_IP, socket.IP_DROP_MEMBERSHIP, membership)
            responder.close()
    log.append('PASS: discovery and TCP integration checks.')
except Exception as error:
    log.append('FAIL / environment limitation: ' + repr(error))
    raise
finally:
    for process in processes:
        if process.poll() is None:
            process.terminate()
            process.wait(timeout=5)
    Path(__file__).with_name('minh_chung.txt').write_text('\n'.join(log) + '\n', encoding='utf-8')
print(log[-1])
