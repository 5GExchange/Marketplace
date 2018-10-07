import nfsapi
import re, os
import json


DOCKER_HOST = os.environ["DOCKER_HOST"] 

class NFSImageError(Exception):
    def __init__(self, error):
        self.error = error


class NFSImageUploadError(NFSImageError):
    pass


class NFSImageDeleteError(NFSImageError):
    pass


class NFSImagesListError(NFSImageError):
    pass


class NFSImagesNotFoundError(NFSImageError):
    pass


class NFSImage:
    #provider_id = 0
    image_name = ''
    image_url = ''
    image_version = ''

    def __init__(self, provider_id, image_name, image_version, image_url, vnfd_id=[]):
        self.provider_id = provider_id
        self.image_name = image_name
        self.image_version= image_version
        #self.nfs_image_name = '%s-%s' % (provider_id, image_name)
        self.nfs_image_name = image_name
        if image_version == 'v1':
            self.nfs_image_url = nfsapi.get_image_url(self.nfs_image_name)
        else:
            self.nfs_image_url = image_url
        self.nfs_vnfd_id = vnfd_id

    def upload(self, image_path, md5sum, image_type='qcow2'):
        code, resp = nfsapi.upload_image(self.nfs_image_name, image_path=image_path, provider_id=self.provider_id, md5sum=md5sum, image_type=image_type)
        if code is not 201:
            raise NFSImageUploadError(resp)
        else:
            print 'Image successfully uploaded!'

    def delete(self):
        code, resp = nfsapi.delete_image(self.nfs_image_name)
        if code is not 204:
            raise NFSImageDeleteError(resp)
        else:
            print 'Image successfully deleted!'


def get_images(provider_id=None):
    code, resp = nfsapi.get_images_list()
    print resp
    if code is not 200:
        raise NFSImagesListError(resp)
    else:
        print 'Images successfully listed!'
        images_list = []
        for image in resp['files']:
            images_list.append(NFSImage(0, image['name'], 'v1', '', image['vnfd_id']))
        #retrieve docker images
        code2, docker_repos = nfsapi.get_docker_repositories('/_catalog', 'registry:catalog:*')
        if code2 is not 200:
            print "ERROR"
            raise NFSImagesListError(docker_repos)
        else:
            print "repo list: ", docker_repos
            code3, docker_list = nfsapi.get_docker_images_list('/tags/list', docker_repos) 
            print "DOCKER_RESP: " + json.dumps(docker_list)
            for repo in docker_list:
                for tag in repo['tags'] or []:
                    images_list.append(NFSImage(1000, repo['name'], tag, DOCKER_HOST + '/' + repo['name']+':'+tag, []))
            return images_list


def get_image(nfs_image_name, provider_id=None):
    code, resp = nfsapi.get_images_list()
    if code is not 200:
        raise NFSImagesListError(resp)
    else:
        print 'Images successfully listed!'
        for image in resp['files']:
            results = re.search('^(?P<provider_id>[0-9]+)-(?P<image_name>.*)$', image['name'], re.IGNORECASE)
            if results and (not provider_id or provider_id == int(results.group('provider_id'))):
                img = NFSImage(int(results.group('provider_id')), results.group('image_name'), 'v1', '', image['vnfd_id'])
                if img.nfs_image_name == nfs_image_name:
                    return img
        raise NFSImagesNotFoundError('Image %s Not Found' % nfs_image_name)


# image = get_image('23-ubuntu.img')
# print image
# images = get_images()
#
# for i in images:
#     print i.nfs_image_url
#     print i.provider_id
#     print i.image_name
#     print i.nfs_vnfd_id

# image = NFSImage(24, 'ubuntu.img')
#
# try:
#     image.upload('file.img')
# except NFSImageUploadError as e:
#     print e.error

# image.delete()



#
# print image.provider_id
# print image.image_name
# print image.nfs_image_name
# print image.nfs_image_url
