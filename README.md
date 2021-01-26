# group9

<h1>Erklärung zu unserem Vorgehen in der Methode TCPClientHandler.run()</h1>

<p>
Beim Werfen der ClassNotFoundException haben wir uns dazu entschieden, die Verbindung zum Client abzubrechen und den TCPClienHandler heruntergefahren. Unsere Überlegung dazu war, dass man nicht wissen kann, welche Art von Message man genau nicht empfangen konnte, also man kann nicht wissen, was genau der Client damit bezwecken wollte. Es könnte ja sein, dass der Client ein weiteres Directory zur Bearbeitung übergeben wollte, aber einen Fehler dabei begangen hat. Deshalb haben wir gesagt, dass das Endergebnis, also das Histogram, durch das Verpassen einer Message möglicherweise korrupt oder falsch sein könnte und wollen deshalb dann die Verbindung abbrechen, weil der eigentliche Sinn des Servers nicht mehr erfüllt wird.
</p>

<p>
Alternativ hätte man auch die Verbindung aufrecht erhalten, die "verpasste" Message ignorieren und danach regulär weitermachen können. Unserer Meinung nach ist das aber nicht sinnvoll, weil man nicht genau wüsste, was die Intention der verpassten Message gewesen wäre. Selbst wenn man das Ergebnis mit Hinweis auf mögliche Fehler darin an den Client senden würde, wäre dem Client ja mit einem falschen Ergebnis nicht mehr geholfen als mit einer abgebrochenen Verbindung.
</p>


