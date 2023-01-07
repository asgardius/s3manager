package asgardius.page.s3manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder>{
    ArrayList Img, Name;
    Context context;

    // Constructor for initialization
    public Adapter(Context context, ArrayList Img, ArrayList Name) {
        this.context = context;
        this.Img = Img;
        this.Name = Name;
    }
    @NonNull
    @Override
    public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_list_item, parent, false);

        // Passing view to ViewHolder
        Adapter.ViewHolder viewHolder = new Adapter.ViewHolder(view);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(@NonNull Adapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        int res = (int) Img.get(position);
        holder.images.setImageResource(res);
        holder.text.setText((CharSequence) Name.get(position));
    }
    @Override
    public int getItemCount() {
        return Img.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView images;
        TextView text;

        public ViewHolder(View view) {
            super(view);
            images = itemView.findViewById(R.id.image);
            text = itemView.findViewById(R.id.imageinfo);
        }
    }
}