#!/bin/bash


# Change this to your netid
netid=rtr100020

#
# Root directory of your project
PROJDIR=$HOME/CS_6378/Project3

#
# This assumes your config file is named "config.txt"
# and is located in your project directory
#
CONFIG=$PROJDIR/config.txt
CONFIG1=$PROJDIR/config1.txt

#
# Directory your java classes are in
#
BINDIR=$PROJDIR

#
# Your main project class
#
PROG=Main

n=0

cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read i
    echo $i
    while read line 
    do
        host=$( echo $line | awk '{ print $1 }' )
        ssh $netid@$host java -cp $BINDIR $PROG $CONFIG $CONFIG1 $n > output$n.txt &
        n=$(( n + 1 ))
    done
   
)


