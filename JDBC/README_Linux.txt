Here is how to compile JDBCng and test it :

1) Checkout (or update) the project from SVN :
svn checkout ...
OR
cd JDBC/
svn update

2) Compile the project :
cd JDBC/
make clean ; make all

3) Run all tests in tests.lst :
make testall DBMSHOST=localhost
(for the board)
make testall DBMSHOST=192.168.0.20

3') Or alternately run a specific test :
make test TEST=test.jdbc.INRIA_1.Test DBMSHOST=localhost
(for the board)
make test TEST=test.jdbc.INRIA_1.Test DBMSHOST=192.168.0.20

4) Enjoy ;)
