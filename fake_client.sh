#!/bin/sh
JARS=libs/asm-4.0.jar:libs/jsonbeans-0.2.jar:libs/kryo-2.20.jar:libs/kryonet-2.18.jar:libs/minlog-none-1.2.jar:libs/objenesis-1.2.jar:libs/reflectasm-1.07.jar

java -cp $JARS:bin/classes/ edu.vub.at.commlib.FoldingClient 192.168.0.149
