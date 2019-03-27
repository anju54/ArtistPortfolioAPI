package com.project.artistPortfolio.ArtistPortfolio.service;

import java.util.List;

import com.project.artistPortfolio.ArtistPortfolio.model.Color;

public interface ColorService {
	
	void create(Color color);
	Color getColorById(int id);
	List<Color> getAllColors();
	void deleteColorById(int id);
	void update(int id, Color color);
	Color getColorByName(String name);

}
