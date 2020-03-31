from fuzzywuzzy import fuzz, process
from fuzzysearch import find_near_matches
from sys import argv
import re
from bib import helper
import time
from collections import OrderedDict
import click
import warnings

def decor(corpus, query, func, ngram_size):
    result = OrderedDict()
    for pos, charAt in enumerate(query):
        ngram = query[pos:pos+ngram_size]
        if len(ngram) == ngram_size:
            result_entry = func(corpus, ngram)
            result.update(result_entry)
    return result


def splitit(corpus, query, ngram=5):
    for pos, charA in enumerate(query):
        ngram = query[pos:pos+ngram]
        if re.search(ngram, corpus):
            for m in re.finditer(ngram, corpus):
                if ngram not in dict.keys():
                    dict.update({ngram: [m.span()]})
                else:
                    dict[ngram] = dict[ngram].append(m.span())


def fuzzy_extracts(qs, ls, threshold=20):
    '''
    fuzzy matches 'qs' in 'ls' and returns list of
    tuples of (word,index)
    '''
    for word, _ in process.extractBests(qs, (ls,), score_cutoff=threshold):
        matchentry = find_near_matches(qs, word, max_l_dist=2, max_deletions=0, max_insertions=0, max_substitutions=2)
        return matchentry


def fuzzy_search_ngram(corpus, query):
    resultin = {query: []}
    resultin[query] = fuzzy_extracts(query, corpus)
    return resultin


def findpath(orderDict):
    result = OrderedDict()
    for ngram in orderDict.keys():
        matches = orderDict[ngram]
        try:
            for match in matches:
                if ngram not in result.keys():
                    result.update({ngram: [(match.start, match.end)]})
                else:
                    result[ngram].append((match.start, match.end))
        except TypeError:
            result.update({ngram: None})
    return result


def sort_od(orderDict):
    swapped = {}
    for item in orderDict:
        if orderDict[item]:
            for match in orderDict[item]:
                swapped.update({match: item})
    startArr = []
    for item in orderDict.values():
        if item:
            for start, end in item:
                startArr.append(start)
    return sorted(getLongestSeq(startArr))


def getLongestSeq(a):
    s = sorted(set(a))
    seq = []
    best = []
    for i in s:
        nexti = i
        if nexti not in seq:
            while nexti in s:
                seq.append(nexti)
                nexti += 1
        seq = set(seq)
        if len(seq) > len(best):
            best = seq
        seq = []

    return best


def maxOverlap(startArr, endArr):
    n = len(startArr)
    maxStart = max(startArr)
    maxEnd = max(endArr)
    maxC = max(maxStart, maxEnd)
    #print(maxC)
    x = (maxC + 2)*[0]
    cur = 0; idx = 0
    for i in range(0, n):
        x[startArr[i]] += 1
        x[endArr[i]] -= 1
    maxy = -1
    for i in range(0, maxC + 1):
        cur += x[i]
        if maxy < cur:
            maxy = cur
            idx = i
    return idx, maxy


@click.command()
@click.option("--n-gram", "-n", default=5, help="The length of the n-gram that is used for the search, less then 4 may result in a very long computation time", type=click.IntRange(4, 20), show_default=True)
@click.option("--path", "-p", help="The path of the searchtext/searchdirectory", required=True, type=click.Path())
@click.option("--query", "-q", help="The searchquery", required=True, type=str)
def main(n_gram, path, query):
    startT = time.perf_counter()
    text = helper.handlePath(path)
    if type(text) == list:
        bestId = 0
        best = []
        for singleText in text:
            text = helper.getbook(singleText)
            result = decor(text, query, fuzzy_search_ngram, int(n_gram))
            result = findpath(result)
            candidate = list(sort_od(result))
            if len(candidate) > len(best):
                best = candidate
                bestId = singleText
    else:
        bestId = text
        text = helper.getbook(text)
        result = decor(text, query, fuzzy_search_ngram, int(n_gram))
        result = findpath(result)
        best = list(sort_od(result))
    if bestId:
        text = helper.getbook(bestId)
    endT = time.perf_counter()
    try:
        print("Searchphrase: " + query + "\n" + "Hit: " + text[best[0]:(best[-1] + int(n_gram))] + "\n" + "at pos: " + str(best[0]) + "-"  + str(best[-1]) +"\n" + "In: " + str(round((endT - startT), 4)) +" seconds" + "\n" + "in: " + bestId)
    except IndexError:
        warnings.warn("Kein Ergebnis gefunden")

if __name__ == "__main__":
    main()
