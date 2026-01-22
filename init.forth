\ Vim Clone - Forth Initialisierung
\ Dieses File wird beim Start geladen

\ === Datei-Operationen ===
\ w, q, wq sind bereits als Built-ins definiert

\ Speichern mit Bestätigung
: ws w "Datei gespeichert" echo ;

\ === Bewegungs-Makros ===

\ Zum Zeilenanfang und in Insert-Mode
: I line-start insert-mode ;

\ Zum Zeilenende und in Insert-Mode (append)
: A line-end insert-mode ;

\ === Nützliche Kombinationen ===

\ Zeile löschen (vereinfacht)
: dd line-start line-end delete-forward ;

\ Wort vorwärts (vereinfacht - bewegt 5 Zeichen)
: word-forward cursor-right cursor-right cursor-right cursor-right cursor-right ;

\ Wort rückwärts (vereinfacht - bewegt 5 Zeichen)
: word-backward cursor-left cursor-left cursor-left cursor-left cursor-left ;

\ === Fenster/Workspace ===

\ Workspace wechseln
: ws-next next-workspace ;

\ === Hilfe ===
: help "Forth: w q wq | if else then | do loop | begin until" echo ;

\ === Rechner-Beispiele ===
\ Beispiel: 2 3 + .  => zeigt 5
\ Beispiel: 10 2 / . => zeigt 5

\ === Kontrollstrukturen Beispiele ===
\ if-else-then:   5 3 > if "ja" else "nein" then echo
\ do-loop:        5 0 do dup . 1+ loop    (zählt 0-4)
\ begin-until:    0 begin dup . 1+ dup 5 = until  (zählt 0-4)

\ === Beispiel-Words mit Kontrollstrukturen ===

\ Prüft ob Zahl positiv ist
: positive? 0 > if "positiv" else "nicht positiv" then echo ;

\ Fakultät (iterativ): n -- n!
: factorial dup 1 > if dup 1- factorial * then ;

\ Mehrfach Cursor nach rechts bewegen: n --
: rights 0 do cursor-right loop ;

\ Mehrfach Cursor nach unten bewegen: n --
: downs 0 do cursor-down loop ;

\ === Benutzerdefinierte Words können hier hinzugefügt werden ===
