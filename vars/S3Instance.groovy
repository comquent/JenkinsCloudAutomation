import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder

import com.amazonaws.services.s3.model.ListBucketsRequest

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
import com.cloudbees.plugins.credentials.CredentialsProvider

def setCredential(value) {
	credential = value
}

def withCredential(value) {
	credential = value
	return this
}

def getClient() {
    withCredentials([
        usernamePassword(credentialsId: credential, usernameVariable: 'accessKey', passwordVariable: 'secretAccessKey')
    ]) {

		def credentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretAccessKey))
		return AmazonS3ClientBuilder.standard().withCredentials(credentials).build()
	}
}

def listStorages() {
	def names = []
	buckets = getClient().listBuckets()
	buckets.each{
		names << it
	}
	return names
}

def createStorage(name) {
	def client = getClient();
//	if(client.doesBucketExist(name) == false) {
		return client.createBucket(name)
//	}
//	return "storage exists"
}

def deleteStorage(name) {
	def client = getClient();
	if(client.doesBucketExist(name) == true) {
		client.deleteBucket(name)
	}
}

def updloadFile(storageName, path, fileName) {
	client = getClient();
	client.putObject(storageName, fileName, path)
}

def listFiles(storageName) {
	def files = []
	client = getClient();
	client.listObjects(bucket_name).getObjectSummaries().each{
		files << it.getKey()
	}
	return files
}

def downloadFile(storageName, fileName) {
	client = getClient();

    stream = client.getObject(bucket_name, key_name).getObjectContent();
    fos = new FileOutputStream(new File(key_name));
    byte[] read_buf = new byte[1024];
    read_len = 0;
    while ((read_len = stream.read(read_buf)) > 0) {
        fos.write(read_buf, 0, read_len);
    }
    stream.close();
    fos.close();	
}

def deleteFile(storageName, fileName) {
	client = getClient();
	client.deleteObject(storageName, fileName)
}


def call(count = 1, body) {
	def config = [:]
	body.resolveStrategy = Closure.DELEGATE_FIRST
	body.delegate = config
	
}
