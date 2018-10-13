package net.aicee.popularmoviesstagetwo.adapter;
//
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import net.aicee.popularmoviesstagetwo.R;
import net.aicee.popularmoviesstagetwo.databinding.ItemVideoBinding;
import net.aicee.popularmoviesstagetwo.model.Video;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoAdapterViewHolder> {

    private final Context movieContext;
    private List<Video> movieList;

    public VideoAdapter(Context context) {
        this.movieContext = context;
    }

    @NonNull
    @Override
    public VideoAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVideoBinding itemVideoBinding = ItemVideoBinding.inflate(LayoutInflater.from(movieContext), parent, false);
        return new VideoAdapterViewHolder(itemVideoBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoAdapterViewHolder videoAdapterViewHolder, int position) {
        Video video = movieList.get(position);
        videoAdapterViewHolder.bind(video);
    }

    @Override
    public int getItemCount() {
        while (movieList == null) {
            return 0;
        }
        return movieList.size();
    }

    public void addVideosList(List<Video> videosList) {
        movieList = videosList;
        notifyDataSetChanged();
    }

    public ArrayList<Video> getList() {
        return (ArrayList<Video>) movieList;
    }

    public class VideoAdapterViewHolder extends RecyclerView.ViewHolder {
        ItemVideoBinding binding;

        VideoAdapterViewHolder(ItemVideoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Video video) {
            binding.setVideo(video);
            binding.setPresenter(this);

            String photoUrl = String.format("https://img.youtube.com/vi/%s/0.jpg", video.videoUrl);
            Picasso.get()
                    .load(photoUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .into(binding.videoIv);
        }

        public void onClickVideo(String videoUrl) {
            Intent appIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("vnd.youtube:" + videoUrl));

            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=" + videoUrl));
            try {
                movieContext.startActivity(appIntent);
            } catch (ActivityNotFoundException ex) {
                movieContext.startActivity(webIntent);
            }
        }
    }
}