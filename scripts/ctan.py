import requests
import codecs
from bs4 import BeautifulSoup


# FUNCTIONS #

def fetch_packages(letter):
    resp = requests.get("https://www.ctan.org/pkg/:" + letter)
    if resp.ok:
        htmlContent = resp.text
        soup = BeautifulSoup(htmlContent, "html.parser")
        packageLinks = soup.select("div.pkg-cols a")
        result = []
        for link in packageLinks:
            name = link.text.replace("­", "")
            result.append(name)
        return result
    return list()


# MAIN #

print("CTAN Fetcher 1.0 (Ruben Schellekens)\n")

count = 0
allPackages = []
sep = ""
file = codecs.open("packages.txt", "w", "utf-8")
for char in "ABCDEFGHIJKLMNOPQRSTUVWXYZ":
    print("Fetching packages starting with {}..".format(char))
    for pack in fetch_packages(char):
        count += 1
        file.write(sep + pack)
        sep = ";"
file.close()

print("\nDone! Found {} packages.".format(count))