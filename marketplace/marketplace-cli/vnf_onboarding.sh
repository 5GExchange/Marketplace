#!/bin/sh

print_usage() {
    echo "Usage: ./vnf_onboarding.sh [options] [vnf_json...]"
    echo "\nLoads VNFs into the marketplace\n"
    echo "Options:"
    echo "  -h  print this help"
    echo "  -H  marketplace proxy host; defaults to localhost"
}

MARKETPLACE_HOST=localhost

while getopts ":hH:" OPT; do
    case $OPT in
    h)
        print_usage
        exit 0
        ;;
    H)
        MARKETPLACE_HOST=$OPTARG
        ;;
    \?)
        echo "Invalid option -$OPTARG"
        print_usage
        exit 1
        ;;
    esac
done

shift $(($OPTIND - 1))

echo "Uploading dummy VNF image..."
curl -X POST -H "Content-type: multipart/form-data" -H "MD5SUM: 1276481102f218c981e0324180bafd9f" -H "Provider-ID: 4" -H "Image-Type: qcow2" -F "file=@dummy_image.qcow2" -w "\n" $MARKETPLACE_HOST/NFS/files

sed -i "s/^ip=.*$/ip=$MARKETPLACE_HOST/" marketplace-cli.conf

for JSON in $@; do
    echo "Uploading VNF `basename $JSON`..."
    python marketplace-cli.py --upload vnfd $JSON
done
