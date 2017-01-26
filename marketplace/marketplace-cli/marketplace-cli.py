import argparse
import ConfigParser
import requests, json
import os

config = ConfigParser.ConfigParser()
config.read('marketplace-cli.conf')

USE_HTTPS = False
MARKETPLACE_IP = config.get("marketplace", "ip")
MARKETPLACE_PORT = int(config.get("marketplace", "port"))

parser = argparse.ArgumentParser(description='Marketplace-CLI v0.1')
parser.add_argument('--upload', type=str, choices=["nsd", "vnfd"])
parser.add_argument("descriptor")
args = parser.parse_args()


def _get_call_url(endpoint):
    protocol = 'https' if USE_HTTPS else 'http'
    return '%s://%s:%s/%s' % (protocol, MARKETPLACE_IP, MARKETPLACE_PORT, endpoint)


def _get_auth_token(username, password):
    r = requests.post(_get_call_url('auth/'), data={'username': username, 'password': password})
    if r.status_code == 200:
        return r.json()['token']

def _vnf_present(vnf_list, vnfd):
    for vnf in vnf_list:
        #print "%s:%s - %s:%s" % (vnf['name'], vnfd['name'], vnf['provider'], vnfd['provider'])
        if (vnf['name'] == vnfd['name']) and (vnf['provider'] == vnfd['provider']):
            return True
    return False
        


if args.upload == "vnfd":
    FP_USERNAME = config.get("fp", "username")
    FP_PASSWORD = config.get("fp", "password")
    FP_AUTH_TOKEN = _get_auth_token(FP_USERNAME, FP_PASSWORD)

    if not FP_AUTH_TOKEN:
        print "FP authentication failed."
        exit(1)

    VNFD_FILE = args.descriptor #config.get("templates", "vnfd")
    if os.path.isfile(VNFD_FILE):
        VNFD_TEMPLATE = open(VNFD_FILE).read()
        vnfd_json = json.loads(VNFD_TEMPLATE)
        get = requests.get(_get_call_url('vnfs/'), headers={"Authorization": "JWT " + FP_AUTH_TOKEN, 'Content-Type': 'application/json'})
        vnf_list = json.loads(get.text)
        if _vnf_present(vnf_list, vnfd_json):
            print "VNFD with name \"%s\" and provider \"%s\" already present." % (vnfd_json['name'], vnfd_json['provider'])
            exit(1)
            
        #print json.dumps(vnfd_json)
        r = requests.post(_get_call_url('vnfs/'), data=json.dumps(vnfd_json), headers={"Authorization": "JWT " + FP_AUTH_TOKEN, 'Content-Type': 'application/json'})
        if r.status_code != 201:
            print "VNFD uploading failed."
            print "STATUS_CODE:" + str(r.status_code)
            print "---ERROR---\n" + r.text
            exit(1)
        else:
            print "VNFD succesfully uploaded."
    else:
        print "VNFD template not found."
        exit(1)

if args.upload == "nsd":

    SP_USERNAME = config.get("sp", "username")
    SP_PASSWORD = config.get("sp", "password")
    SP_AUTH_TOKEN = _get_auth_token(SP_USERNAME, SP_PASSWORD)

    if not SP_AUTH_TOKEN:
        print "SP provider authentication failed."
        exit(1)

    NSD_FILE = config.get("templates", "nsd")
    if os.path.isfile(NSD_FILE):
        NSD_TEMPLATE = open(NSD_FILE).read()
        r = requests.post(_get_call_url('service-catalog/service/catalog'), data=NSD_TEMPLATE, headers={'Content-Type': 'application/json'})
        if r.status_code != 201:
            print "NSD uploading failed."
            print "STATUS_CODE:%s" % r.status_code
            print "---ERROR---\n" + r.text
            exit(1)
        else:
            nsd = r.json()
            print "NSD succesfully uploaded."
            print "NSD_ID:%s" % nsd['nsd']['id']

    else:
        print "NSD template not found."
        exit(1)


