import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials

import com.amazonaws.services.ec2.model.DescribeInstancesRequest
import com.amazonaws.services.ec2.model.DescribeInstancesResult

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials
import com.cloudbees.plugins.credentials.CredentialsProvider

def listInstances(credentials) {

    withCredentials([
        usernamePassword(credentialsId: credentials, usernameVariable: 'accessKey', passwordVariable: 'secretAccessKey')
    ]) {

		def credentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretAccessKey))
		def client= AmazonEC2ClientBuilder.standard().withCredentials(credentials).build()
		DescribeInstancesRequest request = new DescribeInstancesRequest()
		
		DescribeInstancesResult result = client.describeInstances(request)
		
		echo result
	}
}
