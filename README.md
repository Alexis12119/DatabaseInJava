# Instructions

- Install [mariadb-jdbc](https://aur.archlinux.org/packages/mariadb-jdbc).
- Run the following in the mysql shell:

```sh
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' IDENTIFIED BY 'your_password' WITH GRANT OPTION;
FLUSH PRIVILEGES;
```

- Make sure to change this in `config.inc.php`.

```php
$cfg['Servers'][$i]['password'] = 'your_password';
```

- Then run the last command, but make sure to change according to your path.

```
javac EmployeeLoanApp.java
java -classpath /usr/share/java/mariadb-jdbc/mariadb-java-client.jar:. EmployeeLoanAppGUI
```
