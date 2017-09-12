# Crosswork

Crosswork is a crossword-making utility to help build crosswords. This project was inspired while I was working as an English teacher in Seoul, and needed to create new English-based activities for one of my smartest classes, who found simple word searches and other games too easy. Being a casual crossword enthusiast myself, I tried to whip up a simple crossword for my class by hand using Excel, but found out it took forever to get the words to complement each other just right, and found myself brainstorming words and looking them up in the dictionary, as well as using a combination of different tools online to think of words that will fit in particular spaces. I then set out to build a tool to make crosswords that had all the functionality I found myself frustrated not to have on hand.

## Features

### Letter locking

Usage: Press enter to lock in/unlock a letter, press shift+enter to lock in/unlock a word.

One feature was to distinguish between words and letters that I definitely wanted in the puzzle, and letters and words that were candidates but prone to change. Placing words in a puzzle is tricky, since each letter of the word influences the letters of another word in the perpendicular direction. Because of this, I constantly needed to backtrack and redo words-- or sometimes entire sections of puzzles-- to accommodate other words. I found I was confident in some word placements sometimes, and other times I was almost sure I would need to change the word in the future but wanted it "temporarily put in" so I could see how the puzzle would potentially develop with it. But while looking at the puzzle in construction, I found I needed a way to remember more readily which words I was more willing to change than others.

All letters are inputted as unlocked, and are light gray. Locked letters are a solid black. This gives a feel similar to entering letters in by pencil first, then inking them in once you're sure about them. You can still change locked and unlocked letters in the same manner, but there are subtle functional differences to them as will be mentioned below.

### Autocomplete

Usage: Highlight a word, and press spacebar.

Autocomplete was the obvious feature to provide myself. Given a word with some blanks and some fixed letters, I need to know what letters can validly go into the blanks and form a proper word.

![Autocomplete](http://www.moonrabbitgames.net/misc/1.png)

In this example, I want to know all the 4-letter words that will fit here whose second letter is A. I can scroll through my choices on the right, and preview them on the board as unlocked letters. BAFT might not be a good choice here, because it will be tough finding a 4-letter word that ends in "FP", so this previewing makes it easy to see that BAFT is a poor choice.

Autocomplete treats all unlocked letters in the highlighted range as blanks. For example, if I had the word "REAP" going down next to "TEA", with "REA" being unlocked, then autocompleting in this highlighted range would give the same results-- it would not consider all 4-letter words with "AA" as the second and third letter, as the "A" in the third square would be unlocked. 

### Single-letter queries

Usage: Highlight a square, and press "?" (i.e. shift+slash)

Sometimes I wanted to know which letters could go into a square, and still provide valid words for the row and column it resides in.

![Single-letter queries](http://www.moonrabbitgames.net/misc/2.png)

In this example, I'm curious what letter can go after this T, without shooting myself in the foot for the downwards word ending in P. Maybe I'm thinking about what vowel will fit in nicely; can I put in the word "TEA"? After the query, it looks like not-- the only vowel I can put is A, which indicates that any other vowel I put in will lead to zero valid words in the downwards direction (i.e. no 4-letter English words that start with E and end in P). In fact, my choices for the second letter of this across word is quite limited, so this makes it easy to decide.

### Autosuggest

Usage: Press INSERT while autocomplete list is populated, or press shift+space instead of just space for autocomplete.

This is more or less a combination of the two above ideas; what words can I put here that follow the given constraints, but also provide valid letter placements for its perpendicular neighbors?

![Autosuggest](http://www.moonrabbitgames.net/misc/3.png)

I needed a 3-letter word here that started with "T", and first thought of words like "TEA", "TOE", and "THE". As mentioned above, doing a single-letter query on the second square showed me that "TEA" and "TOE" were invalid placements, since "E" or "O" in the second square wouldn't yield any valid English words going in the downwards direction. So why suggest them at all in the autocomplete?

Autosuggest eliminates words in the autocomplete list that place letters with no valid results in the perpendicular direction, and puts them at the bottom of the list with a warning. Additionally, it sorts the remainder of the words, such that the top word yields the most valid English words for all perpendicular neighbors. The suggestion of "TSK" implies that I'll have an easy time finding matching the downward words: "S__P" and "K__E", whereas "TWO" is placed a lot lower on the list, suggesting that maybe there are only very few words for either "W__P" or "O__E" in the downwards direction (likely the latter).

### Console queries

Usage: Click on the text box below the autocomplete list, and type in a query, using "*" as blanks.

Before I made Crosswork, I would use regex-like utilities online to see what words I can make with certain restrictions. This is still necessary even with Crosswork, as sometimes I want to place multi-word clues "METOO", and "ATTHETIME" in the example puzzle provided.

![Console queries](http://www.moonrabbitgames.net/misc/4.png)

In this example, I'm considering breaking this 9-letter word going across into a 4-letter word and a 5-letter word. To see if this is viable, I want to see what words I can make with "RA__N", but Crosswork will only suggest 9-letter words to me here using autocomplete and autosuggest. So I type in the query by hand in the mini-console. The suggestion "RAMEN" makes me wonder if I can find a clue to the solution "WARMRAMEN"... maybe "Japanese comfort food perfect for a cold, winter day"?

### Display numbers

Usage: Press F5 to toggle

![Display numbers](http://www.moonrabbitgames.net/misc/5.png)

When I was using Crosswork, I was using it just as a tool for creating the answer key-- I still used Excel to create the final product, as it was more flexible layout-wise, and I could also work on them in the office. It's really easy to make mistakes while doing the numbering by hand, and if you miss the numbering for one square, then you have to redo all the numbers after it. I decided to implement the algorithm in Crosswork to use it to guide my numbering, but also it leaves room in the future for me to implement clue-construction in Crosswork as well, making it a full crossword-making utility.

### Construction mode

Usage: Press F6 to toggle

![Construction mode](http://www.moonrabbitgames.net/misc/6.png)

A feature of crosswords that some people overlook is that the layout of them is symmetrical along the diagonal axes. Crosswork enforces that symmetry in its construction mode, which again, helps prevent construction mistakes.

## Other controls

* **Undo** - ctrl+z
* **Redo** - ctrl+shift+z
* **Save state** - shift+any number key
* **Load state** - any number key
* **Save to current slot** - ctrl+S
* **Next/previous word** - tab
* **Beginning/end of word** - home/end
* **Change highlight orientation** - shift+up/down/left/right
