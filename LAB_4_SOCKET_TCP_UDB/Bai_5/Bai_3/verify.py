"""Chay tu thu muc goc sau khi bien dich Java: python Bai_5/Bai_3/verify.py."""
import datetime
import os
from pathlib import Path
import queue
import socket
import subprocess
import threading

settings = subprocess.run(['java', '-XshowSettings:properties', '-version'],
                          capture_output=True).stderr.decode('utf-8', errors='replace')
java_home = next(row.split('=', 1)[1].strip() for row in settings.splitlines()
                 if row.strip().startswith('java.home ='))
JAVA = str(Path(java_home) / 'bin' / ('java.exe' if os.name == 'nt' else 'java'))
LOG = []
PROCESSES = []


def launch(name, *args):
    process = subprocess.Popen(
        [JAVA, '-Dstdout.encoding=UTF-8', '-Dstderr.encoding=UTF-8',
         'Bai_5.Bai_3.' + name, *map(str, args)],
        stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
        encoding='utf-8', bufsize=1)
    PROCESSES.append(process)
    lines = queue.Queue()
    def read_output():
        for line in process.stdout:
            lines.put(line.rstrip())
    threading.Thread(target=read_output, daemon=True).start()
    return process, lines


def line(lines):
    result = lines.get(timeout=8)
    LOG.append(result)
    return result


def send(process, command):
    LOG.append('Client sends: ' + repr(command))
    process.stdin.write(command + '\n')
    process.stdin.flush()


def stop(process):
    process.terminate()
    process.wait(timeout=5)


def check_date(response, command):
    formats = {'DATE': '%d %m %Y', 'TIME': '%H %M %S',
               'DATETIME': '%d %m %Y %H %M %S'}
    value = response.removeprefix('Server: ')
    parsed = datetime.datetime.strptime(value, formats[command])
    assert parsed.strftime(formats[command]) == value, response


try:
    # Chon mot cong trong cho tung giao thuc.
    for protocol, sock_type in [('TCP', socket.SOCK_STREAM), ('UDP', socket.SOCK_DGRAM)]:
        with socket.socket(socket.AF_INET, sock_type) as probe:
            probe.bind(('127.0.0.1', 0))
            port = probe.getsockname()[1]
        LOG.append('\n=== ' + protocol + ' ===')
        server, server_lines = launch(protocol.title() + 'DateTimeServer', port)
        assert 'server' in line(server_lines)
        client, client_lines = launch(protocol.title() + 'DateTimeClient', '127.0.0.1', port)
        line(client_lines)
        for command in ['DATE', 'TIME', 'DATETIME']:
            send(client, command)
            check_date(line(client_lines), command)
        for command in ['', 'UNKNOWN', ' date ']:
            send(client, command)
            assert line(client_lines) == 'Server: ERR UNKNOWN_COMMAND'
        if protocol == 'TCP':
            send(client, 'QUIT')
            assert line(client_lines) == 'Server: BYE'
            assert client.wait(timeout=5) == 0
            client, client_lines = launch('TcpDateTimeClient', '127.0.0.1', port)
            line(client_lines)
            send(client, 'DATE')
            check_date(line(client_lines), 'DATE')
        else:
            send(client, 'QUIT')
            assert line(client_lines) == 'Server: ERR UNKNOWN_COMMAND'
        LOG.append('Stop server while client remains active')
        stop(server)
        send(client, 'TIME')
        response = line(client_lines)
        if protocol == 'TCP':
            assert any(text in response for text in ['đóng kết nối', 'Lỗi kết nối', 'Hết thời gian chờ']), response
            assert client.wait(timeout=5) == 0
        else:
            assert 'Hết thời gian chờ 3 giây' in response, response
            assert client.poll() is None
            # Khoi dong lai server, client cu van gui duoc yeu cau moi.
            server, server_lines = launch('UdpDateTimeServer', port)
            line(server_lines)
            send(client, 'DATETIME')
            check_date(line(client_lines), 'DATETIME')
            client.stdin.close()
            assert client.wait(timeout=5) == 0
            stop(server)
    LOG.append('\nPASS: formats, unknown commands, TCP QUIT, server interruption, UDP recovery.')
finally:
    for process in PROCESSES:
        if process.poll() is None:
            stop(process)
    Path(__file__).with_name('minh_chung.txt').write_text('\n'.join(LOG) + '\n', encoding='utf-8')

print(LOG[-1])
