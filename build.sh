#!/bin/bash

rm -r "target"
/home/user/.jdks/corretto-17.0.10/bin/java -Dmaven.multiModuleProjectDirectory=/home/user/source/NiceChat -Djansi.passthrough=true -Dmaven.home=/usr/lib/intellij-idea/plugins/maven/lib/maven3 -Dclassworlds.conf=/usr/lib/intellij-idea/plugins/maven/lib/maven3/bin/m2.conf -Dmaven.ext.class.path=/usr/lib/intellij-idea/plugins/maven/lib/maven-event-listener.jar -javaagent:/usr/lib/intellij-idea/lib/idea_rt.jar=45803:/usr/lib/intellij-idea/bin -Dfile.encoding=UTF-8 -classpath /usr/lib/intellij-idea/plugins/maven/lib/maven3/boot/plexus-classworlds.license:/usr/lib/intellij-idea/plugins/maven/lib/maven3/boot/plexus-classworlds-2.7.0.jar org.codehaus.classworlds.Launcher -Didea.version=2023.3.4 package

if [ -z $1 ]; then
  exit 0
elif [ "$1" == "test" ]; then
  cp target/NiceChat-* "/home/user/Desktop/servertest/plugins/nicechat.jar"
  exit 0
fi
