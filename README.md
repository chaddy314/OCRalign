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
  
  
