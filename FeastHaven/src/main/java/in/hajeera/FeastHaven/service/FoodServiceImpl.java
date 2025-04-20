package in.hajeera.FeastHaven.service;

import in.hajeera.FeastHaven.entity.FoodEntity;
import in.hajeera.FeastHaven.io.FoodRequest;
import in.hajeera.FeastHaven.io.FoodResponse;
import in.hajeera.FeastHaven.repository.FoodRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FoodServiceImpl implements FoodService{

    @Autowired
    private S3Client s3Client;
    @Autowired
    private FoodRepository foodRepository;
    @Value("${aws.s3.bucketname}")
    private String bucketName;

    @Value("${aws.access.key}")
    private String accessKey;

    @Value("${aws.secret.key}")
    private String secretKey;

    @Override
    public String uploadFile(MultipartFile file) {
        String filenameExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);
        String key = UUID.randomUUID().toString()+"."+filenameExtension;
        // Detect dummy key and return mocked URL
        if (isDummyCredentials(accessKey, secretKey)) {
            return "https://" + bucketName + ".s3.amazonaws.com/" + key;
        }

        try{
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .acl("public-read")
                    .contentType(file.getContentType())
                    .build();
            PutObjectResponse response = s3Client.putObject(putObjectRequest,RequestBody.fromBytes(file.getBytes()));
            if(response.sdkHttpResponse().isSuccessful()){
                return "https://"+bucketName+".s3.amazonaws.com/"+key;
            }else{
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"File upload failed");
            }
        }catch(IOException ex){
           throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"An error occured while uploading the file");
        }
    }

    private boolean isDummyCredentials(String accessKey, String secretKey) {
        // we can tweak this to match any other fake keys we want
        return accessKey.startsWith("AKIA2QDX") && secretKey.startsWith("FI0AE+R8");
    }

    @Override
    public FoodResponse addFood(FoodRequest request, MultipartFile file) {
        FoodEntity newFoodEntity = convertToEntity(request);
        String imageUrl = uploadFile((file));
        newFoodEntity.setImageUrl(imageUrl);
        // Save image data since we are mocking
        if (isDummyCredentials(accessKey, secretKey)) {
            try {
                newFoodEntity.setImageData(file.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        newFoodEntity= foodRepository.save(newFoodEntity);
        return convertToResponse(newFoodEntity);
    }

    @Override
    public List<FoodResponse> readFoods() {
        List<FoodEntity> databaseEntries = foodRepository.findAll();
        return databaseEntries.stream().map(object -> convertToResponse(object)).collect(Collectors.toList());
    }

    @Override
    public FoodResponse readFood(String id) {
       FoodEntity existingFood = foodRepository.findById(id).orElseThrow(() -> new RuntimeException("Food not found for the id:"+id));
    return convertToResponse(existingFood);
    }

    @Override
    public boolean deleteFile(String filename) {
        if (isDummyCredentials(accessKey, secretKey)) {
            System.out.println("delete: " + filename);
            return true;
        }

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(filename)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
        return true;
    }

    @Override
    public void deleteFood(String id) {
       FoodResponse response = readFood(id);
       String imageUrl = response.getImageUrl();
       String filename = imageUrl.substring(imageUrl.lastIndexOf("/")+1);
       boolean isFileDelete = deleteFile(filename);
       if(isFileDelete){
           foodRepository.deleteById(response.getId());
       }

    }

    private FoodEntity convertToEntity(FoodRequest request){
        return FoodEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .price((request.getPrice()))
                .build();
    }

    private FoodResponse convertToResponse(FoodEntity entity){

        String base64Image = null;

        if (isDummyCredentials(accessKey, secretKey) && entity.getImageData() != null) {
            base64Image = "data:image/jpeg;base64," +
                    Base64.getEncoder().encodeToString(entity.getImageData());
        }
        return FoodResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .price(entity.getPrice())
                .imageUrl(entity.getImageUrl()) // Use URL only
                .imageBase64(base64Image)
                .build();
    }
}
