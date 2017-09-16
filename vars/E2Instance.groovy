import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials

import com.amazonaws.services.ec2.model.DescribeInstancesRequest
import com.amazonaws.services.ec2.model.DescribeInstancesResult

def listInstances() {

	def credentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretAccessKey))
    def client= AmazonEC2ClientBuilder.standard().withCredentials(credentials).build()
	DescribeInstancesRequest request = new DescribeInstancesRequest()
	
	RunInstancesResult result = client.runInstances(request)
	
	echo result
}