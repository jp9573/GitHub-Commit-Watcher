package in.co.jaypatel.githubcommitwatcher;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommitAdapter extends RecyclerView.Adapter<CommitAdapter.MyViewHolder> {

    private List<Commit> commitList;
    private Context mContext;
    private DateFormat mediumDateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, new Locale("EN", "en"));

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView name, date, message, email, userName;
        CircleImageView photo;
        CardView cardView;

        MyViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.card_view);
            name = view.findViewById(R.id.tvName);
            date = view.findViewById(R.id.tvDate);
            message = view.findViewById(R.id.tvMessage);
            email = view.findViewById(R.id.tvEmail);
            photo = view.findViewById(R.id.avatarPhoto);
            userName = view.findViewById(R.id.tvUserName);
        }
    }

    public CommitAdapter(Context context, List<Commit> commitList) {
        this.mContext = context;
        this.commitList = commitList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_commit, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        final Commit commit = commitList.get(position);
        holder.name.setText(commit.getName());
        holder.email.setText(commit.getEmail());
        holder.message.setText(commit.getMessage());

        holder.date.setText(mediumDateFormat.format(commit.getDate()));

        holder.userName.setText(commit.getUserName());

        Glide.with(mContext)
                .load(commit.getAvatarUrl())
                .apply(RequestOptions.circleCropTransform())
                .into(holder.photo);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessageDialog(commit.getMessage());
            }
        });
    }

    private void showMessageDialog(String message) {
        new AlertDialog.Builder(mContext)
                .setTitle("Commit Message")
                .setIcon(R.drawable.ic_message_black_24dp)
                .setMessage(message)
                .setNegativeButton(R.string.close, null)
                .show();
    }

    @Override
    public int getItemCount() {
        return commitList.size();
    }

}
