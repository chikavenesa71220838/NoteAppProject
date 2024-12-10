import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.lammoire.R

class ImageAdapter(private val imagePaths: MutableList<String>, private val noteId: String?, private val onDelete: (Int) -> Unit) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imagePath = imagePaths[position]
        val bitmap = BitmapFactory.decodeFile(imagePath)
        holder.imageView.setImageBitmap(bitmap)

        holder.itemView.setOnLongClickListener {
            onDelete(position)
            true
        }
    }

    override fun getItemCount(): Int {
        return imagePaths.size
    }
}