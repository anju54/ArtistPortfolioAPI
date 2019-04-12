package com.project.artistPortfolio.ArtistPortfolio.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RestController;

import com.project.artistPortfolio.ArtistPortfolio.DTO.ArtistProfileDTO;
import com.project.artistPortfolio.ArtistPortfolio.DTO.MediaDTO;
import com.project.artistPortfolio.ArtistPortfolio.DTO.ProfileDTO;
import com.project.artistPortfolio.ArtistPortfolio.model.ArtistProfile;
import com.project.artistPortfolio.ArtistPortfolio.model.Media;
import com.project.artistPortfolio.ArtistPortfolio.model.PaintingType;
import com.project.artistPortfolio.ArtistPortfolio.model.UserModel;
import com.project.artistPortfolio.ArtistPortfolio.service.ArtistProfileService;
import com.project.artistPortfolio.ArtistPortfolio.service.UserService;
import com.project.artistPortfolio.ArtistPortfolio.service.Impl.MediaServiceImpl;

@RestController
@CrossOrigin
@RequestMapping("/api/artist-profile")
public class ArtistProfileController {
	
	@Autowired
	ArtistProfileService artistProfileService;
	
	@Autowired
	private UserService userService;
	
	private final static Logger logger = LoggerFactory.getLogger(ArtistProfileController.class);
	
	/**
	 * This is used to get artist profile pic path by profile id
	 * @param id
	 * @return Media object
	 */
	@GetMapping("/profile-pic")
	public Media getProfilePicByArtistProfileId(Authentication authentication) {
		
		String email = userService.getPrincipalUser(authentication).getUsername();
		UserModel user = userService.getUserByEmail(email);
		int id = user.getId();   // user id
		
		return artistProfileService.getProfilePicByArtistProfileId(id);
	}
	
	@GetMapping("/public/profile-pic")
	public Media getPublicProfilePic(@RequestParam("id") int artistProfileId) {
		
		Media media = artistProfileService.getArtistProfileById(artistProfileId).getMedia();
		return media;
	}
	
	/**
	 * This is used to get artist profile information that will be display over 
	 * artist profile page 
	 * @param artistProfileId
	 * 		artist Profile id.
	 * @param profileName
	 * 		artist profile name.
	 */
	@GetMapping("/info")
	public ProfileDTO getArtistProfile(@RequestParam("id") int artistProfileId) {
		
		return artistProfileService.getArtistPublicProfileInfo(artistProfileId);
	}
	
	@GetMapping("/secured/info/{email}")
	public ProfileDTO getArtistProfileData(@PathVariable("email") String email) {
		
		int artistProfileId = userService.getUserByEmail(email).getArtistProfile().getId();
		return artistProfileService.getArtistPublicProfileInfo(artistProfileId);
	}
	
	/**
	 * This is used to display all the painting type of any artist by id.
	 * @param id
	 * 		artist profile id.
	 */
	@GetMapping("/{id}/all/painting_type")
	public  List<PaintingType>  getArtistProfilePaintingByid(@PathVariable("id") int id) {
		
		return artistProfileService.getListOfPaintingType(id);
	}
		
	@GetMapping("/{id}")
	public ArtistProfile getArtistProfileByid(@PathVariable("id") int id) {
		
		return artistProfileService.getArtistProfileById(id);
	}
	
	@DeleteMapping("/{id}")
	public void deleteArtistProfile(int id) {
		
		artistProfileService.deleteByid(id);
	}
	
	@PostMapping("/basic-info")
	public void create(@RequestBody ArtistProfileDTO artistProfileDTO) {
		
		artistProfileService.createArtistProfileRecord(artistProfileDTO);
	}
	
	@PostMapping("/link/media")
	public void createLinkArtistProlfileMedia(@RequestBody MediaDTO mediaList,@RequestParam("profileName")
																					String profileName) {
		artistProfileService.addArtistProfileMedia(mediaList, profileName);
	}

	@PutMapping("/basic-info/{email}")
	public void update(@RequestBody ArtistProfileDTO artistProfileDTO,@PathVariable("email") String email) {
		
		artistProfileService.updateArtistProfileRecord(artistProfileDTO,email);
	}
	
	@GetMapping("/loggedIn/{email}")
	public int getArtistProfileId(@PathVariable("email") String email){
		int artistProfileId = 0;
		try {
			 artistProfileId =  userService.getUserByEmail(email).getArtistProfile().getId();
			return artistProfileId;
		}catch (Exception e) {
			logger.info(e.getMessage());
		}
		return artistProfileId;
		
	}
}
