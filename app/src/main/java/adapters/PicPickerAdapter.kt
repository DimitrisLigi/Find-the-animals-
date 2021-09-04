package adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.dimitrisligi.findtheanimals.R
import gameinterfaces.ChoosePhotoImageClickListener
import models.BoardSize
import kotlin.math.min

class PicPickerAdapter(
    private val context: Context,
    private val listOfPics: List<Uri>,
    private val boardSize: BoardSize,
    private val choosePhotoImageClickListener: ChoosePhotoImageClickListener):
    RecyclerView.Adapter<PicPickerAdapter.ViewHolder> (){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PicPickerAdapter.ViewHolder {

        val v = LayoutInflater.from(context).inflate(R.layout.image_card_picker,parent,false)
        val imageCardWidth = parent.width / boardSize.getWidth()
        val imageCardHeight = parent.height / boardSize.getHeight()
        val imageCardLength = min(imageCardHeight,imageCardWidth)
        val layoutParams = v.findViewById<ImageView>(R.id.ivCustomImage).layoutParams
        layoutParams.width = imageCardLength
        layoutParams.height = imageCardLength

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: PicPickerAdapter.ViewHolder, position: Int) {
        if(position < listOfPics.size){
            holder.bind(listOfPics[position])
            }else{
                holder.bind()
            }
    }

    override fun getItemCount(): Int = boardSize.getPairs()

    inner class ViewHolder(view: View):RecyclerView.ViewHolder(view){
        private val ivCustomImage: ImageView = itemView.findViewById(R.id.ivCustomImage)

        fun bind(uri: Uri){
            ivCustomImage.setImageURI(uri)
            ivCustomImage.setOnClickListener(null)
        }

        fun bind(){
            ivCustomImage.setOnClickListener {
                //Launch event for user to select photos
                //Invoking
                choosePhotoImageClickListener.onPlaceholderClicked()
            }
        }
    }
}