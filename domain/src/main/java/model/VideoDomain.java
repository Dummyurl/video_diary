package model;

/**
 * Created by Terry on 11/6/2016.
 * Video model in domain layer
 */

public class VideoDomain {
    private int id;
    private String videoUrl;
    private String imageUrl;
    private String title;
    private String description;
    private boolean isDeletedVideo;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public int getId() {
        return id;
    }

    public String getVideoPath() {
        return videoUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDeletedVideo() {
        return isDeletedVideo;
    }

    public void setDeletedVideo(boolean deletedVideo) {
        isDeletedVideo = deletedVideo;
    }
}
