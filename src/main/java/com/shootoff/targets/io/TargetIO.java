package com.shootoff.targets.io;

import java.io.File;
import java.util.List;
import java.util.Optional;

import com.shootoff.gui.TargetEditorController;
import com.shootoff.targets.EllipseRegion;
import com.shootoff.targets.ImageRegion;
import com.shootoff.targets.PolygonRegion;
import com.shootoff.targets.RectangleRegion;
import com.shootoff.targets.TargetRegion;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;

public class TargetIO {
	public static final double DEFAULT_OPACITY = 0.7;
	
	public static void saveTarget(List<Node> regions, File targetFile) {
		RegionVisitor visitor;
		
		if (targetFile.getName().endsWith("target")) {
			visitor = new XMLTargetWriter(targetFile);
		} else {
			System.err.println("Unknown target file type.");
			return;
		}
		
		for (Node node : regions) {
			TargetRegion region = (TargetRegion)node;
			
			switch (region.getType()) {
			case IMAGE:
				ImageRegion img = (ImageRegion)node;
				visitor.visitImageRegion(img.getBoundsInParent().getMinX(), img.getBoundsInParent().getMinY(), 
						img.getImageFile(), img.getAllTags());
				break;
			case RECTANGLE:
				RectangleRegion rec = (RectangleRegion)node;
				visitor.visitRectangleRegion(rec.getBoundsInParent().getMinX(), rec.getBoundsInParent().getMinY(), 
						rec.getWidth(), rec.getHeight(), 
						TargetEditorController.getColorName((Color)rec.getFill()), 
						rec.getAllTags());
				break;
			case ELLIPSE:
				EllipseRegion ell = (EllipseRegion)node;
				double absoluteCenterX = ell.getBoundsInParent().getMinX() + ell.getRadiusX();
				double absoluteCenterY = ell.getBoundsInParent().getMinY() + ell.getRadiusY();
				visitor.visitEllipse(absoluteCenterX, absoluteCenterY, 
						ell.getRadiusX(), ell.getRadiusY(), 
						TargetEditorController.getColorName((Color)ell.getFill()), 
						ell.getAllTags());
				break;
			case POLYGON:
				PolygonRegion pol = (PolygonRegion)node;
				
				Double[] points = new Double[pol.getPoints().size()];
				
				for (int i = 0; i < pol.getPoints().size(); i+=2) {
					Point2D p = pol.localToParent(pol.getPoints().get(i), 
												  pol.getPoints().get(i + 1));
					
					points[i] = p.getX();
					points[i + 1] = p.getY();
				}
				
				visitor.visitPolygonRegion(
						points, 
						TargetEditorController.getColorName((Color)pol.getFill()), 
						pol.getAllTags());
				break;
			}
		}
		
		visitor.visitEnd();
	}
	
	public static Optional<Group> loadTarget(File targetFile) {
		List<Node> regions;
		
		if (targetFile.getName().endsWith("target")) {
			regions = new XMLTargetReader(targetFile).load();
		} else {
			System.err.println("Unknown target file type.");
			return Optional.empty();
		}
		
		Group targetGroup = new Group();
		for (Node region : regions) {
			region.setOpacity(DEFAULT_OPACITY);
			targetGroup.getChildren().add(region);
		}
		
		return Optional.of(targetGroup);
	}
}
