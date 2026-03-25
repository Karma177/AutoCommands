# AutoCommands

AutoCommands è un plugin per server proxy basato su [Velocity](https://papermc.io/software/velocity) che permette di eseguire automaticamente liste di comandi predeterminate al verificarsi di vari eventi per gli utenti che ne fanno parte.
E' molto utile se devi assegnare rank, comunicare su tutto il network un gradito join, o eseguire operazioni di backend.

I tre eventi attuali supportati di esecuzione su cui scaturisce il run dei comandi inseriti sono:
- Al **login** (sul proxy) `onJoin`
- Su **chiamata manuale del comando** `onCommand`
- Al **logout** (disconnessione dal proxy): `onLogout`


## Comandi disponibili
Di seguito la lista dei comandi che possono essere chiamati (in console o come player) per interagire col plugin.

| Comando | Descrizione |
| ----------- | ----------- |
| `/autocommands <uuid>` | Forza manualmente l'esecuzione dei comandi inseriti per un determinato UUID, leggendo l'albero `"onCommand":[]` del suo JSON. |
| `/autocmd <uuid>` | Alias ridotto del comando precedente. |


## Permessi necessari
Onde evitare abusi, solo chi è munito del relativo set di permessi può attingere all'esecuzione dei comandi da game. I permessi base di installazione sono i seguenti:

| Permesso | Utilità | Modalità default |
| ----------- | ----------- | ----------- |
| `autocommands.admin` | Garantisce l'accesso e l'uso manuale al comando ed abilita l'auto completamento del TAB per l'eventuale parsing dell'argomento `<uuid>`. | `FALSE`


## TODO
Un breve registro con piani e lavori per le patch o integrazioni in cantiere in futuro.

- [x] Aggiunta del commando `/autocommands reload`.
- [x] Aggiunta del commando `/autocommands help`.
- [x] Aggiunta del commando `/autocommands version`.
- [/] Macro?
    - Complete al 90%, manca solo testing
- [x] Eliminare EventMethod, passare direttamente a chiavi "join", "command", "leave" (sono utilizzati solo nel codice, direi che sono ricordabili...)
- [x] Pulizia generale
    - [x] Messages sotto utils
    - [x] PermissionCheck sotto Utils
    - [x] Refactor e pulizia del codice
- [x] Format messaggi in chat di default

