package alchemy.utils;

public class WardrobeUtils {

    private final String endpoint;
    private final String publicBucket;
    private final String privateBucket;

    public WardrobeUtils(String endpoint, String publicBucket, String privateBucket) {
        this.endpoint = endpoint;
        this.publicBucket = publicBucket;
        this.privateBucket = privateBucket;
    }

    public String getPublicImageUrl(String object) {
    	return new StringBuilder().append(endpoint).append("/").append(publicBucket).append("/").append(object).toString();
    }
    
    public String getPrivateImageUrl(String object) {
    	return new StringBuilder().append(endpoint).append("/").append(privateBucket).append("/").append(object).toString();
    }
    
}