echo "input src file name (except .tex)"
read FILE
platex ${FILE}.tex
echo "continue? (y/n)"
read OPERATION
if [ $OPERATION -eq "n" ]; then
    exit 0
else
    dvipdfmx ${FILE}.dvi
    xdg-open ${FILE}.pdf
fi
echo "fin."
