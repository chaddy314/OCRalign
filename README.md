# OCRalign
Aligns OCR lines with ground truth lines from recognized sources

Contributors:

  [Yannik Herbst](https://github.com/Kolophonium0) (Python Search Script)

Installation:
  1. Download Release and unpack
  2. Open Terminal and change to directory you unpacked the contents of the zip file
  3. Run with:
    ```
    $java -jar OCRalign.jar
    ```
    
If you want to use the Ground Truth Search function:  
  1. make sure to have Python 3.8 installed (earlier versions not tested):
  ```
  $ python3 -V
  Python 3.8.2
  ```
  2. Install required python tools
  ```
  $ pip3 install fuzzywuzzy fuzzysearch click python-Levenshtein
  ```

Usage:
  
  ```
  usage: ocralign [-l <ocrdata> <gtdata> | -x <pagexml> <gt.txt> | -b
       <folder> | -s <pagexml> <corpus folder>]    [-i] [-sm] [-a] [-y]
       [-n] [-c] [-w]
 -l,--line-mode <ocrdata> <gtdata>            aligns two strings
 -x,--xml-mode <pagexml> <gt.txt>             aligns every line from
                                              pageXml to corresponding
                                              gt.txt
 -b,--batch-mode <folder>                     Aligns every file in folder
 -s,--search-mode <pagexml> <corpus folder>   searches every line of xml
                                              in specified corpus folder
 -i,--insert                                  [OPTIONAL] writes every gt
                                              line in xml line
 -sm,--safemode                               [OPTIONAL] Only reads XML
                                              file(s)
 -a,--shortmode                               [OPTIONAL] Abbreviates
                                              output
 -y,--no-confirm                              [OPTIONAL] Automatically
                                              confirms and writes every
                                              line to xml
 -n,--ngram                                   [OPTIONAL] The length of the
                                              n-gram that is used for the
                                              search, less then 4 may
                                              result in a very long
                                              computation time
 -c,--check                                   [OPTIONAL] Only writes if
                                              similarity > c (default:0.5)
 -w,--wodiak                                  [OPTIONAL] use gt.wodiak.txt
```
