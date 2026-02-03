# Projekt został stworzony na systemie Windows

## Komendy do włączenia programu:
### Kompilacja w Maven dla graficznej opcji:
```bash 
mvn clean compile exec:java "-Dexec.mainClass=project.go.connection.GoServer"
```
### Włączenie jednego klienta oraz bota (opcjonalnie):
``` bash
mvn javafx:run
mvn exec:java:run -Pbot
```
## Diagramy UML zostały wykonane w PlantUML
