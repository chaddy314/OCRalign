from os.path import isfile, isdir, join
from os import walk


def getbook(bookid):
    with open(bookid) as infile:
        book = infile.read()
    return book

def handlePath(path):
    if isdir(path):
        pathList = []
        for root, dirs, files in walk(path):
            for file in files:
                text = join(root, file)
                pathList.append(text)
        return pathList
    else:
        return path

def readBooks(path, query, func):
    if isfile(path):
        book = {path: {
            'text': getbook(path),
            'ngrams': func(getbook(path)),
        }}
        return book
    elif isdir(path):
        booklist = {}
        for roots, dirs, files in walk(path):
            for file in files:
                book = getbook(join(roots, file))
                booklist.update({join(roots, file): {
                    'text': book,
                    'ngrams': func(book)
                }
                })
        return booklist