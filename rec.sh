echo "input file name :"
read NAME
echo "time :"
read TIME
rec -r 16k -b 16 -c 1 $NAME trim 0 $TIME
echo "finish."
