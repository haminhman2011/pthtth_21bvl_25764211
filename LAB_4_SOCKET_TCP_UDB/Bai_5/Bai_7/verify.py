"""Chay tu thu muc goc sau khi bien dich Java. Logs thu nghiem nam trong thu muc tam."""
import concurrent.futures
import datetime
import os
from pathlib import Path
import socket
import subprocess
import tempfile

settings = subprocess.run(['java', '-XshowSettings:properties', '-version'], capture_output=True).stderr.decode('utf-8', errors='replace')
home = next(s.split('=', 1)[1].strip() for s in settings.splitlines() if s.strip().startswith('java.home ='))
java = str(Path(home) / 'bin' / ('java.exe' if os.name == 'nt' else 'java'))
log = []
clients = []


class Client:
    def __init__(self, port):
        self.socket = socket.create_connection(('127.0.0.1', port), timeout=5)
        self.reader = self.socket.makefile('r', encoding='utf-8')
        self.remote = '/127.0.0.1:' + str(self.socket.getsockname()[1])
        clients.append(self)

    def expect(self, message, expected):
        self.socket.sendall((message + '\n').encode('utf-8'))
        response = self.reader.readline().rstrip('\r\n')
        assert response == expected, (message, response, expected)

    def close(self):
        self.reader.close()
        self.socket.close()


with tempfile.TemporaryDirectory(prefix='bai7_logs_') as temporary:
    directory = Path(temporary) / 'data' / 'logs'
    with socket.socket() as probe:
        probe.bind(('127.0.0.1', 0))
        port = probe.getsockname()[1]
    server = subprocess.Popen([java, '-Dstdout.encoding=UTF-8', 'Bai_5.Bai_7.LogServer', str(port), str(directory)], stdout=subprocess.PIPE, stderr=subprocess.DEVNULL)
    try:
        log.append(server.stdout.readline().decode('utf-8').strip())
        a = Client(port)
        a.expect('content before HELLO', 'ERR HELLO_REQUIRED')
        for candidate in ['', '../outside', 'a/b', 'a\\b', 'a.b', 'two words', 'name!', 'name\t']:
            a.expect('HELLO ' + candidate, 'ERR INVALID_CLIENT_ID')
        assert not list(directory.iterdir())
        log.append('Reject missing HELLO and invalid IDs; no files created [PASS]')
        a.expect('HELLO client-01_A', 'OK HELLO client-01_A')
        content = ['Xin chào Việt Nam', 'HET', 'nội dung có HET ở giữa', '', 'A\tB\\C', 'QUIT nằm trong nội dung']
        for message in content:
            a.expect(message, 'OK LOGGED')
        a.expect('QUIT', 'BYE')
        assert a.reader.readline() == ''
        first = directory / 'client-01_A.txt'
        rows = first.read_text(encoding='utf-8').splitlines()
        assert len(rows) == len(content)
        for row, message in zip(rows, content):
            timestamp, remote, escaped = row.split('\t')
            datetime.datetime.fromisoformat(timestamp)
            assert remote == a.remote, (remote, a.remote)
            assert escaped == message.replace('\\', '\\\\').replace('\t', '\\t')
        log.append('UTF-8, HET, empty content, escaped tabs, exact QUIT; timestamp and remote verified [PASS]')
        log.append('\nActual log entries:\n' + '\n'.join(rows))

        # Cung clientId duoc phep dung nhieu ket noi; ghi dong nguyen ven.
        group = [Client(port) for _ in range(4)]
        for client in group:
            client.expect('HELLO shared', 'OK HELLO shared')
        def write_messages(item):
            index, client = item
            for i in range(50):
                client.expect(f'message-{index}-{i}', 'OK LOGGED')
            client.expect('QUIT', 'BYE')
        with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:
            list(pool.map(write_messages, enumerate(group)))
        shared = (directory / 'shared.txt').read_text(encoding='utf-8').splitlines()
        assert len(shared) == 200
        parsed = [row.split('\t') for row in shared]
        assert all(len(parts) == 3 for parts in parsed)
        assert {parts[2] for parts in parsed} == {f'message-{j}-{i}' for j in range(4) for i in range(50)}
        log.append('4 concurrent connections sharing one ID: 200 intact entries, no loss [PASS]')

        b = Client(port)
        b.expect('HELLO abrupt', 'OK HELLO abrupt')
        b.expect('saved before disconnect', 'OK LOGGED')
        b.close()
        c = Client(port)
        c.expect('HELLO client-01_A', 'OK HELLO client-01_A')
        c.expect('append after reconnect', 'OK LOGGED')
        c.expect('QUIT', 'BYE')
        assert len(first.read_text(encoding='utf-8').splitlines()) == len(content) + 1
        log.append('Abrupt disconnect keeps saved log; reconnect appends instead of overwriting [PASS]')

        # Loi ghi file khong duoc tra OK va khong lam server dung.
        (directory / 'blocked.txt').mkdir()
        d = Client(port)
        d.expect('HELLO blocked', 'OK HELLO blocked')
        d.expect('cannot save', 'ERR LOG_WRITE')
        d.expect('QUIT', 'BYE')
        log.append('File write failure returns ERR LOG_WRITE; server continues [PASS]')

        session = subprocess.run([java, '-Dstdout.encoding=UTF-8', '-Dstderr.encoding=UTF-8', 'Bai_5.Bai_7.LogClient', '127.0.0.1', str(port)], input='HELLO demo\nHET\nXin chào\nQUIT\n'.encode('utf-8'), capture_output=True, timeout=10)
        output = session.stdout.decode('utf-8')
        assert session.returncode == 0 and not session.stderr, session.stderr
        assert 'OK HELLO demo' in output and output.count('OK LOGGED') == 2 and 'BYE' in output
        assert len((directory / 'demo.txt').read_text(encoding='utf-8').splitlines()) == 2
        log.append('\nActual LogClient session [PASS]:\n' + output)
        log.append('PASS: all logging checks.')
    finally:
        for client in clients:
            client.close()
        server.terminate()
        server.wait(timeout=5)
        Path(__file__).with_name('minh_chung.txt').write_text('\n'.join(log) + '\n', encoding='utf-8')

print(log[-1])
