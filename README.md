# GraphServer
This application can process and analyse data produced by a Supply Chain Management (SCM) application as described in the paper: D. Bleco, Y. Kotidis “Graph Analytics on Massive Collections of Small Graphs,”, EDBT.
and is part of my master thesis in Information Systems at Athens University of Economics and Business.

It is developed in Java and the GUI in Swing. The database that uses is MonetDB. https://www.monetdb.org/

# Database Installation
1. Download MonetDB from the official site: https://www.monetdb.org/Downloads
2. Run the installer and select the default installation.

# Run the Application
1. After the installation of MonetDB, run the the MonetDB SQL Server and the MonetDB SQL Client. Create a database called 'monetdb', using the MonetDB SQL Client.
2. In Intellij import project as Java Project and run the App.java

# Usage
After all the above steps are completed, the application welcomes us in a Swing GUI. 
1. Choose Session -> New (left up corner)
2. Import Data: In this window we upload our files located in the File Samples folder. Of course you can create your own, based on these samples.
3. After the import of the files, we now can see the topology that we imported into the application. User can select one or more edges with the mouse and then must press the 'Show Records' button.
4. Then a new topology loaded based on the selected edges. Now user has the ability to select a record id from the right panel + an aggregation function.
5. The result of this operation is located in the right up corner and user can select again the same or other record if and aggregation function.
6. Also, user has the ability to click on a Node or an Edge and get information about them.

# Appendix
https://openproceedings.org/2014/conf/edbt/BlecoK14.pdf
