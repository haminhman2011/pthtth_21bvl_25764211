"""Chay tu thu muc goc sau khi bien dich. Dung upload va file mau trong thu muc tam."""
import base64
import hashlib
import os
from pathlib import Path
import socket
import shutil
import struct
import subprocess
import tempfile
import zlib

ROOT = Path.cwd()
settings = subprocess.run(['java', '-XshowSettings:properties', '-version'], capture_output=True).stderr.decode('utf-8', errors='replace')
home = next(row.split('=', 1)[1].strip() for row in settings.splitlines() if row.strip().startswith('java.home ='))
JAVA = str(Path(home) / 'bin' / ('java.exe' if os.name == 'nt' else 'java'))
LOG = []


def wire_string(text):
    data = text.encode('utf-8')
    return struct.pack('>i', len(data)) + data


def read_exact(connection, count):
    data = bytearray()
    while len(data) < count:
        chunk = connection.recv(count - len(data))
        if not chunk:
            raise EOFError('Unexpected connection close')
        data.extend(chunk)
    return bytes(data)


def response(connection):
    length = struct.unpack('>i', read_exact(connection, 4))[0]
    assert 0 <= length <= 1024
    return read_exact(connection, length).decode('utf-8')


def upload(port, name, payload, expected='OK', declared=None, digest=None, early_eof=False):
    with socket.create_connection(('127.0.0.1', port), timeout=5) as connection:
        size = len(payload) if declared is None else declared
        sha = hashlib.sha256(payload).hexdigest() if digest is None else digest
        connection.sendall(wire_string(name) + struct.pack('>q', size) + wire_string(sha))
        actual = response(connection)
        if actual == 'READY':
            # Gui nhieu chunk de kiem tra server khong coi mot read la ca file.
            for offset in range(0, len(payload), 7):
                connection.sendall(payload[offset:offset + 7])
            if early_eof:
                connection.shutdown(socket.SHUT_WR)
            # Ca thanh cong khong dong chieu gui: server khong duoc doi EOF.
            actual = response(connection)
        assert actual == expected, (name, actual, expected)
        LOG.append(f'{name!r}, declared={size}, sent={len(payload)} -> {actual} [PASS]')


with tempfile.TemporaryDirectory(prefix='bai8_file_') as temporary:
    directory = Path(temporary)
    # Classpath tuong doi tranh van de launcher Windows voi duong dan Unicode.
    classes = directory / 'Bai_5/Bai_8'
    classes.mkdir(parents=True)
    for compiled in (ROOT / 'Bai_5/Bai_8').glob('*.class'):
        shutil.copy2(compiled, classes / compiled.name)
    upload_dir = directory / 'Bai_5/Bai_8/upload'
    with socket.socket() as probe:
        probe.bind(('127.0.0.1', 0))
        port = probe.getsockname()[1]
    server = subprocess.Popen([JAVA, '-Dstdout.encoding=UTF-8', '-Dstderr.encoding=UTF-8', '-cp', '.', 'Bai_5.Bai_8.FileServer', str(port)], cwd=directory, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    try:
        LOG.append(server.stdout.readline().decode('utf-8').strip())
        vietnamese = 'Xin chào Việt Nam!\nTruyền file TCP an toàn.\n'.encode('utf-8')
        # Anh PNG 1x1 that, dung byte goc khong can thu vien hinh anh.
        png = base64.b64decode('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+ip1sAAAAASUVORK5CYII=')
        offset = 8
        assert png[:8] == b'\x89PNG\r\n\x1a\n'
        while offset < len(png):
            length = struct.unpack('>I', png[offset:offset + 4])[0]
            chunk = png[offset + 4:offset + 8 + length]
            crc = struct.unpack('>I', png[offset + 8 + length:offset + 12 + length])[0]
            assert zlib.crc32(chunk) == crc
            if chunk[:4] == b'IDAT':
                assert len(zlib.decompress(chunk[4:])) == 3
            offset += length + 12
        samples = {'empty.bin': b'', 'tieng-viet.txt': vietnamese, 'image.png': png}
        for name, content in samples.items():
            upload(port, name, content)
            saved = (upload_dir / name).read_bytes()
            assert saved == content
            LOG.append('Saved SHA-256: ' + hashlib.sha256(saved).hexdigest())
        for name in ['../outside.txt', '..\\outside.txt', '/absolute.txt', 'C:\\outside.txt', 'folder/file.txt', 'CON.txt']:
            upload(port, name, b'attack', 'ERR INVALID_FILENAME')
        assert not (upload_dir.parent / 'outside.txt').exists()
        assert not (directory / 'outside.txt').exists()
        LOG.append('Traversal and absolute paths rejected; no file outside upload [PASS]')
        upload(port, 'bad-hash.txt', b'changed data', 'ERR HASH_MISMATCH', digest='0' * 64)
        assert not (upload_dir / 'bad-hash.txt').exists()
        upload(port, 'short.bin', b'abc', 'ERR INCOMPLETE_FILE', declared=100, early_eof=True)
        assert not (upload_dir / 'short.bin').exists()
        upload(port, 'negative.bin', b'', 'ERR INVALID_SIZE', declared=-1)
        upload(port, 'huge.bin', b'', 'ERR INVALID_SIZE', declared=2**32)
        upload(port, 'invalid-hash.bin', b'', 'ERR INVALID_HASH', digest='xyz')
        upload(port, 'tieng-viet.txt', b'do not overwrite', 'ERR FILE_EXISTS')
        assert (upload_dir / 'tieng-viet.txt').read_bytes() == vietnamese

        source = directory / 'client-vi.txt'
        source.write_bytes(vietnamese)
        client = subprocess.run([JAVA, '-Dstdout.encoding=UTF-8', '-Dstderr.encoding=UTF-8', '-cp', '.', 'Bai_5.Bai_8.FileClient', str(source), '127.0.0.1', str(port)], cwd=directory, capture_output=True, timeout=10)
        output = client.stdout.decode('utf-8')
        assert client.returncode == 0 and not client.stderr, client.stderr
        assert 'Server: OK' in output
        assert (upload_dir / source.name).read_bytes() == vietnamese
        assert not list(upload_dir.glob('.receiving-*.part'))
        LOG.append('\nActual FileClient session [PASS]:\n' + output)
        LOG.append('PASS: empty, UTF-8 text, PNG, traversal, hashes, long sizes, short transfer, no EOF boundary, no overwrite, temp cleanup.')
    finally:
        server.terminate()
        server.wait(timeout=5)
        errors = server.stderr.read().decode('utf-8', errors='replace')
        if errors:
            LOG.append('Server diagnostics:\n' + errors)
        Path(__file__).with_name('minh_chung.txt').write_text('\n'.join(LOG) + '\n', encoding='utf-8')

print(LOG[-1])
