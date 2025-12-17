# Projekt został stworzony na systemie Windows

# Komendy do włączenia programu:

# Kompilacja:
javac -sourcepath src -d bin src\main\java\project\go\game\*.java src\main\java\project\go\connection\*.java 
# Włączenie servera oraz klienta:
java -cp bin project.go.connection.GoServer
java -cp bin project.go.connection.GoClient