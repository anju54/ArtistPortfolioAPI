package com.project.artistPortfolio.ArtistPortfolio.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.artistPortfolio.ArtistPortfolio.DTO.ArtistProfilePic;
import com.project.artistPortfolio.ArtistPortfolio.DTO.MediaArtistDTO;
import com.project.artistPortfolio.ArtistPortfolio.DTO.MediaDTO;
import com.project.artistPortfolio.ArtistPortfolio.DTO.PaintingsDTO;
import com.project.artistPortfolio.ArtistPortfolio.model.ArtistProfile;
import com.project.artistPortfolio.ArtistPortfolio.model.ArtistProfileMedia;
import com.project.artistPortfolio.ArtistPortfolio.model.Media;
import com.project.artistPortfolio.ArtistPortfolio.model.UserModel;
import com.project.artistPortfolio.ArtistPortfolio.repository.ArtistProfileMediaRepository;
import com.project.artistPortfolio.ArtistPortfolio.repository.ArtistProfileRepository;
import com.project.artistPortfolio.ArtistPortfolio.service.ArtistProfileService;
import com.project.artistPortfolio.ArtistPortfolio.service.MediaService;
import com.project.artistPortfolio.ArtistPortfolio.service.MediaStorageService;
import com.project.artistPortfolio.ArtistPortfolio.service.UserService;

@RestController
@CrossOrigin
@RequestMapping("/api/media")
public class MediaController {
	
	private final static Logger logger = LoggerFactory.getLogger(MediaController.class);
	
	@Autowired
	private  MediaStorageService fileStorageService;
	
	@Autowired
	private ArtistProfileRepository artistProfileRepository;
	
	@Autowired
	private ArtistProfileService artistProfileService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private MediaService mediaService;
	
	@Autowired
	ServletContext servletContext;
	
	@Autowired
	private ArtistProfileMediaRepository artistProfileMediaRepository;
	
//	@GetMapping("/artist/albums/{pageNo}/{pageLimit}/{id}")
//	public List<ArtistProfileMedia> getMediaByPageNo(@PathVariable("pageNo") int pageNo,@PathVariable("pageLimit") int pageLimit,@PathVariable("id") int id){
//		
//		//Pageable firstPageWithTwoElements = PageRequest.of(0, 2);
//		
//		List<ArtistProfileMedia> artistProfileMediaList = artistProfileMediaRepository.
//				findArtistProfileMediaByArtistProfileId(id, (Pageable) PageRequest.of(pageNo, pageLimit));
//		return artistProfileMediaList;
//	}
	
	@GetMapping("/all/public/images")
	public List<ArtistProfileMedia> list(@RequestParam("id") int id,@RequestParam("pageNo") int pageNo,@RequestParam("pageLimit") int pageLimit){
		
		return artistProfileMediaRepository.findArtistProfileMediaByArtistProfileIdAndpublicImage(id,(Pageable) PageRequest.of(pageNo, pageLimit));
	}
	
	/**
	 * This is used to get all paintings of particular artist
	 * 
	 * @return list of media object
	 */
	@GetMapping("/artist/albums/{pageNo}/{pageLimit}")
	public List<PaintingsDTO> getMediaByArtistProfileMediaKey(Authentication authentication,Pageable pageable,@PathVariable("pageNo") int pageNo,@PathVariable("pageLimit") int pageLimit){
		
		return mediaService.getMediaByArtistProfileMediaKey(authentication,pageNo,pageLimit);
	}
	
	@GetMapping("/{id}")
	public @ResponseBody Media getMediaById(@PathVariable("id") int id) {
		
		logger.info("trying to get painting type");
		return 	mediaService.getMediaById(id);
	}
	

	/**
	 * This is used to get all paintings of particular artist
	 * 
	 * @return list of media object
	 */
	@GetMapping("/public-albums/artist")
	public MediaArtistDTO getPublicMedia(@RequestParam("id") int artistProfileId,Pageable pageable,@RequestParam("pageNo") int pageNo,@RequestParam("pageLimit") int pageLimit){
		
		return mediaService.getPublicMedia(artistProfileId,pageNo,pageLimit);
	}
	
	@PostMapping(value="/upload/profile-pic", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> uploadFile(MultipartFile file,Authentication authentication,HttpServletRequest request) throws IOException {
		
		String filename = file.getOriginalFilename();
		File a = new File(filename);
		logger.info(a.getAbsolutePath());
		logger.info(a.getCanonicalPath());
		logger.info(filename, a.getAbsoluteFile());
		logger.info(filename);
		
		logger.info("context path printing : "+ request.getContextPath() );
		
		//append server IP and port number to read the image
		String uploadLocation = "../ArtistPortfolioAPI/media/artist-profile-pics/";
		
		String fileType = "profile-pic";
		fileStorageService.uploadFile(file,uploadLocation,fileType);
		
		String email = userService.getPrincipalUser(authentication).getUsername();
		UserModel user  = userService.getUserByEmail(email);
		
		ArtistProfile artistProfile =  new ArtistProfile();
		System.out.println(user);
		artistProfile =  user.getArtistProfile();
		
		Media media = new Media();
		media.setPath("/media/artist-profile-pics/");
		media.setFileName("profile-pic-"+filename);
		media.setFilenameOriginal(filename);
		
		Media savedMedia = mediaService.createMedia(media);
		
//		if(artistProfile!=null) {
//			
//		}
		
		artistProfile.setMedia(savedMedia);
		artistProfileRepository.save(artistProfile);
		
		return new ResponseEntity<>( HttpStatus.OK);
	}
	
	@PostMapping(value="/upload/paintings", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> uploadpaintings(MultipartFile file,Authentication authentication) throws IOException {

		String username = userService.getPrincipalUser(authentication).getUsername();
		UserModel user = userService.getUserByEmail(username);
		String profileName = user.getArtistProfile().getProfileName();
		
		MediaDTO mediaDTO = new MediaDTO();
		
		String paintingUploadLocation = "../ArtistPortfolioAPI/media/" + profileName +"/";
			
		String filename = file.getOriginalFilename();
		logger.info("............................"+filename);
		//String fileType = "paintings";
		
		File newFile;
		String renameFileName =  String.valueOf(System.currentTimeMillis()/1000)  + file.getOriginalFilename();
		newFile = new File(paintingUploadLocation + renameFileName );

		newFile.createNewFile();
			
		// Open output stream to new file and write from file to be uploaded
		FileOutputStream fileOutputStream = new FileOutputStream(newFile);
		fileOutputStream.write(file.getBytes());
		logger.info("closing the file");
		fileOutputStream.close();
		
		//fileStorageService.uploadFile(file,paintingUploadLocation,fileType);
		
		mediaService.thumnailOfImage(paintingUploadLocation, renameFileName);
		
		mediaDTO.setFileName( renameFileName );
		mediaDTO.setPath("/media/"+profileName+"/");
		
		artistProfileService.addArtistProfileMedia(mediaDTO, profileName);
		return null;	
	}
	
	/**
	 * This is used to set image is public or private
	 * @param publicImage
	 * @param file
	 * 	
	 */
	@PutMapping("/isPublic/{isPublic}/{id}")
	public void setPublicOrPrivateImage(@PathVariable("isPublic") String publicImage,@PathVariable("id") int id) {
		
		mediaService.setPublicOrPrivateImage(publicImage, id);		
	}
	
	/**
	 * This is used to update profile picture of particular user
	 * @param email
	 * @param file
	 * @throws IOException
	 */
	@PutMapping("/profile-pic")
	public void updateProfilePic(Authentication authentication,MultipartFile file) throws IOException {
		
		mediaService.updateProfilePic(authentication, file);
	}
	
	/**
	 * This is used to get all the profile pic of artist
	 * @return List of media object
	 */
	@GetMapping("/all/artist/profile-pics/{pageNo}/{pageLimit}")
	public List<ArtistProfilePic> getAllProfilePicOfArtist(@PathVariable("pageNo") int pageNo, @PathVariable("pageLimit") int pageLimit){
		return mediaService.getAllProfilePicOfArtist(pageNo,pageLimit);
	}
	
	@PutMapping("/{id}")
	public void update(@PathVariable("id") int id, @RequestBody Media media) {
		
		mediaService.updateMedia(id, media);
	}
	
	@DeleteMapping("/profile-pic/{email}")
	public String deleteProfilePic(@PathVariable("email") String email) {
		
		ArtistProfile artistProfile = userService.getUserByEmail(email).getArtistProfile();
		mediaService.deleteMediaById(artistProfile.getMedia().getId());
		
		artistProfile.setMedia(null);
		artistProfileRepository.save(artistProfile);
		
		return "profile pic deleted.";
	}
	
//	@GetMapping("/artist-test/albums/{pageNo}/{pageLimit}")
//	public List<PaintingsDTO> getMediaWithSettings(Authentication authentication,Pageable pageable,@PathVariable("pageNo") int pageNo,@PathVariable("pageLimit") int pageLimit){
//		
//		List<PaintingsDTO> dtos = new ArrayList<PaintingsDTO>();
//		
//		int artistProfileId = userService.getUserByEmail( userService.getPrincipalUser
//				(authentication).getUsername() ).getArtistProfile().getId();
//		
//		List<ArtistProfileMedia> artistProfileMediaList = artistProfileMediaRepository.
//				findArtistProfileMediaByArtistProfileId(artistProfileId, (Pageable) PageRequest.of(pageNo, pageLimit));
//		
//		for(ArtistProfileMedia artistProfileMedia: artistProfileMediaList) {
//			
//			PaintingsDTO dto = new PaintingsDTO();
//			dto.setMedia(artistProfileMedia.getMedia());
//			dto.setPublicImage(artistProfileMedia.getPublicImage());
//			dtos.add(dto);
//		}
//		return dtos;
//		
//	}
	
	@DeleteMapping("/{id}")
	public String delete(@PathVariable("id") int id) {
		
		return mediaService.deleteMediaById(id);
	}
}
