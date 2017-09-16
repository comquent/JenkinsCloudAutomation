def listInstances() {

	def credentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretAccessKey))
    def client= AmazonEC2ClientBuilder.standard().withCredentials(credentials).build()
	DescribeInstancesRequest request = new DescribeInstancesRequest()
	
	RunInstancesResult result = client.runInstances(request)
	
	echo result
}