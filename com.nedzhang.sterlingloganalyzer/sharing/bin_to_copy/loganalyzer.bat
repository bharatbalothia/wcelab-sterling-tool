SET JAVA_HOME=c:\java\jdk1.8.0_102

%JAVA_HOME%\bin\java -cp resource;libs/*;com.nedzhang.sterlingloganalyzer.jar -XX:MaxPermSize=256m -Xms512m -Xmx1536m com.nedzhang.sterlingloganalyzer.gui.LogAnalyzerMain
