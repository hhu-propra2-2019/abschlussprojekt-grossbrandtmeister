# Zusammenfassung Vortrag

Der Vortrag zum Thema "Java Api’s - the missing manual" wurde von Hendrik Ebbers gehalten.
Zu Beginn wurden einige Themen zur Auswahl gestellt und es wurde entschieden, welche davon im
Vortag genauer beleuchtet werden sollten.
Für folgende Themen wurde mehrheitlich gevotet:

- Annotations
- Executors
- Stream.error
- HashCode (Bonus)

## Annotations

Das erste Thema befasst sich mit Annotations in Java. Zentrale Punkte waren die Frage, was
Annotations unter der Haube eigentlich sind und wie man eigene Annotations schreiben kann.
Zu ersterem wurden die Herkunft (JRE oder Custom) und der Aufbau, sowie die
Einsatzmöglichkeiten besprochen. Anschließend daran wurde aufgezeigt, wie man Annotations
richtig selbst aufbaut und welche Tricks und Quality of Life Optionen Java den Entwicklern bietet.

## Executors

Anhand des Beispiels eines einfachen Http-Servers ging es bei diesem Thema um die Frage, wie
man in Java Nebenläufigkeit möglichst einfach und ohne massiven Overhead, mithilfe von
Executor Objekten implementiert.
Vorteil gegenüber manuell geschriebenen und verwalteten Threads ist vor allem das nutzen von
Thread Pools, welche es ermöglichen, die Verwaltung und Lastverteilung der Threads, den
Implementierungen des java.lang.concurrent packages zu überlassen und sogar die Anzahl der
Threads dynamisch anpassen zu lassen.

## Stream.error

Da java Streams nativ kein ordentliches Exception-Handling anbieten, muss man andere Lösungen
finden, Fehler und deren Konsequenzen abzufangen und sichtbar zu machen.
Empfohlen wurden hier die Funktionalitäten des **VAVR** packages, welches Konzepte der
funktionalen Programmierung in Java nutzbar macht. Ein Beispiel war hier das Result Objekt.
Zudem wurde gezeigt wie man ein dem Result ähnliches Objekt selber implementiert.

## HashCode

In diesem Thema wurde die hashCode Funktion näher erläutert, welche jedes Objekt in Java besitzt,
um Vergleichbarkeit zwischen Objekten herzustellen.
Zentral ging es hier darum, die hashCode Funktion selbst zu implementieren und was dabei zu
beachten ist. Vor allem das nutzen von mutable Variablen bei der Berechnung kann zu Problemen
führen, da diese den Hash verändern, sollten sie selbst verändert werden.
Als mögliche Lösung kamen hier die von IDE’s automatisch generierbaren hash Funktionen zur
Sprache.

```
1
```

