# EvilShot
This is a scraper for LightShot.com, a screen capture program for Windows and Mac.

The program is known for having a security flaw where it's possible to gather access to all the screenshots that have been posted on their DB by indexing the following URL:

                                                                 https://prnt.sc/XXXXXX
 
 Where the X can be replaced with pretty much any alphanumerical string of six characters.                                                              
 The program counts more than two millions of uploads since 2010, the year it came out, which makes it a huge attack vector for data scraping. 
 
 ## OCR
 This bot uses Object Character Recognition which can be used to parse every image gathered into a stream 
 of characters which can be later on analyzed in order to find predefined keywords.

## How To Use
The bot works in two ways: 

<b>Scrape All Images</b> will return in chat every image gathered from the indexed URL above

<b>Scrape for Word</b> will perform OCR on the scraped images with the word that the user has specified and will only return pictures which contain that predefined word.

## Disclaimer
This is for educational and academic purposes only, I will not be held responsible in the event any criminal charges be brought against any individuals misusing this 
information to break the law.


