import hashlib
import os

def md5_for_file(rootdir, filename, blocksize=2**20):
    m = hashlib.md5()
    with open( os.path.join(rootdir, filename) , "rb" ) as f:
        while True:
            buf = f.read(blocksize)
            if not buf:
                break
            m.update( buf )
    return m.hexdigest()