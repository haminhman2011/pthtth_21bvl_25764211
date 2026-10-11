"""Chay tu thu muc goc sau khi bien dich: python Bai_5/Bai_5/verify.py."""
import concurrent.futures
import os
from pathlib import Path
import socket
import subprocess
import time

settings = subprocess.run(['java', '-XshowSettings:properties', '-version'], capture_output=True).stderr.decode('utf-8', errors='replace')
home = next(s.split('=', 1)[1].strip() for s in settings.splitlines() if s.strip().startswith('java.home ='))
java = str(Path(home) / 'bin' / ('java.exe' if os.name == 'nt' else 'java'))
log = []
connections = []


class Client:
    def __init__(self, port):
        self.socket = socket.create_connection(('127.0.0.1', port), timeout=3)
        self.reader = self.socket.makefile('r', encoding='utf-8')
        connections.append(self)
        assert self.read() == 'NICK_REQUIRED'

    def read(self):
        return self.reader.readline().rstrip('\r\n')

    def send(self, command):
        self.socket.sendall((command + '\n').encode('utf-8'))

    def expect(self, command, expected):
        self.send(command)
        actual = self.read()
        assert actual == expected, (command, actual, expected)
        log.append(repr(command) + ' -> ' + actual + ' [PASS]')

    def close(self):
        self.reader.close()
        self.socket.close()


with socket.socket() as probe:
    probe.bind(('127.0.0.1', 0))
    port = probe.getsockname()[1]
server = subprocess.Popen([java, '-Dstdout.encoding=UTF-8', 'Bai_5.Bai_5.ChatServer', str(port)], stdout=subprocess.PIPE, stderr=subprocess.DEVNULL)
try:
    log.append(server.stdout.readline().decode('utf-8').strip())
    a, b, c = [Client(port) for _ in range(3)]
    a.expect('USERS', 'ERR NICK_REQUIRED')
    a.expect('NICK bad name', 'ERR INVALID_NICK')
    a.expect('NICK alice', 'OK NICK alice')
    b.expect('NICK alice', 'ERR NICK_IN_USE')
    b.expect('NICK bob', 'OK NICK bob')
    c.expect('NICK carol', 'OK NICK carol')
    a.expect('USERS', 'USERS alice bob carol')
    a.expect('MSG Xin chào Việt Nam', 'OK MSG')
    assert b.read() == c.read() == 'MSG alice Xin chào Việt Nam'
    log.append('UTF-8 broadcast to both other clients; sender receives only OK MSG [PASS]')
    a.expect('MSG   ', 'ERR EMPTY_MESSAGE')
    a.expect('UNKNOWN', 'ERR UNKNOWN_COMMAND')

    # Hai nguoi gui dong thoi; moi dong nhan phai con nguyen ven.
    def burst(client, prefix):
        for i in range(20):
            client.send(f'MSG {prefix}{i}')
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
        list(pool.map(lambda item: burst(*item), [(a, 'a'), (b, 'b')]))
    actual = {c.read() for _ in range(40)}
    expected = {f'MSG alice a{i}' for i in range(20)} | {f'MSG bob b{i}' for i in range(20)}
    assert actual == expected
    for client, other in [(a, 'bob b'), (b, 'alice a')]:
        messages = [client.read() for _ in range(40)]
        assert messages.count('OK MSG') == 20
        assert {s for s in messages if s != 'OK MSG'} == {f'MSG {other}{i}' for i in range(20)}
    log.append('Concurrent broadcast: 40 complete messages, no interleaved lines, no self echo [PASS]')

    # Ngat TCP khong gui QUIT.
    b.close()
    for _ in range(50):
        a.send('USERS')
        names = a.read()
        if names == 'USERS alice carol':
            break
        time.sleep(0.02)
    assert names == 'USERS alice carol', names
    log.append('Abrupt disconnect removes bob; server stays active [PASS]')
    d = Client(port)
    d.expect('NICK bob', 'OK NICK bob')
    d.expect('QUIT', 'BYE')
    assert d.read() == ''
    a.expect('MSG still running', 'OK MSG')
    assert c.read() == 'MSG alice still running'
    log.append('Nickname reuse, QUIT closes connection, continued chat [PASS]')

    # Hai ket noi cung dang ky mot nickname: chi mot ket noi thanh cong.
    e, f = Client(port), Client(port)
    with concurrent.futures.ThreadPoolExecutor(max_workers=2) as pool:
        list(pool.map(lambda client: client.send('NICK same'), [e, f]))
    responses = [e.read(), f.read()]
    assert sorted(responses) == ['ERR NICK_IN_USE', 'OK NICK same'], responses
    log.append('Concurrent nickname registration: exactly one winner [PASS]')
    for client in [a, c, e, f]:
        client.expect('QUIT', 'BYE')

    session = subprocess.run([java, '-Dstdout.encoding=UTF-8', '-Dstderr.encoding=UTF-8', 'Bai_5.Bai_5.ChatClient', '127.0.0.1', str(port)], input='NICK tester\nUSERS\nMSG hello\nQUIT\n'.encode('utf-8'), capture_output=True, timeout=8)
    output = session.stdout.decode('utf-8')
    assert not session.stderr and session.returncode == 0, session.stderr
    for text in ['OK NICK tester', 'USERS tester', 'OK MSG', 'BYE']:
        assert text in output, output
    log.append('\nActual ChatClient session [PASS]:\n' + output)
    log.append('PASS: all chat checks.')
finally:
    for client in connections:
        client.close()
    server.terminate()
    server.wait(timeout=5)
    Path(__file__).with_name('minh_chung.txt').write_text('\n'.join(log) + '\n', encoding='utf-8')
print(log[-1])
