DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd $DIR

JAVA_HOME=/home/nzhang/app/jdk1.7.0_72

${JAVA_HOME}/bin/java -cp resource:libs/*:com.nedzhang.sterlingloganalyzer.jar -XX:MaxPermSize=256m -Xms512m -Xmx1536m com.nedzhang.sterlingloganalyzer.gui.LogAnalyzerMain
