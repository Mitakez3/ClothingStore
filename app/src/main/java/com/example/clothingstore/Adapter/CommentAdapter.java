package com.example.clothingstore.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clothingstore.Domain.Comment;
import com.example.clothingstore.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private List<Comment> commentList;
    private Context context;

    public CommentAdapter(List<Comment> commentList, Context context) {
        this.commentList = commentList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        // Hiển thị rating và nội dung bình luận
        holder.tvRating.setText("★ " + comment.getRating());
        holder.tvComment.setText(comment.getContent());

        // Log thông tin bình luận đang được xử lý
        Log.d("CommentAdapter", "Binding comment at position " + position);
        Log.d("CommentAdapter", "CustomerID: " + comment.getCustomerId());

        // Lấy tên người dùng từ Firebase theo customerId
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(comment.getCustomerId());
        Log.d("CommentAdapter", "Fetching user data for customerId: " + comment.getCustomerId());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Kiểm tra xem có username hay không
                String username = snapshot.child("username").getValue(String.class);
                Log.d("CommentAdapter", "Fetched username: " + username);

                if (username != null) {
                    holder.tvUsername.setText(username);  // Hiển thị username
                    Log.d("CommentAdapter", "Username set to: " + username);
                } else {
                    holder.tvUsername.setText("Người dùng không xác định");  // Nếu không có username
                    Log.d("CommentAdapter", "No username found, setting default text.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Nếu có lỗi khi truy vấn dữ liệu, hiển thị thông báo lỗi
                holder.tvUsername.setText("Lỗi tải tên người dùng");
                Log.e("CommentAdapter", "Error fetching username: " + error.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvUsername, tvRating, tvComment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvComment = itemView.findViewById(R.id.tvComment);
        }
    }
}
