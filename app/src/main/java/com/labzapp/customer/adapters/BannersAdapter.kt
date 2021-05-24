package com.labzapp.customer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.labzapp.customer.databinding.BannerListItemBinding
import com.labzapp.customer.fragments.HomeFragment
import com.labzapp.customer.models.BannerData
import com.squareup.picasso.Picasso

class BannersAdapter(val context: Context, private val banners: List<BannerData>) : RecyclerView.Adapter<BannersAdapter.BannerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = BannerListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BannerViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return banners.size
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val bannerp = banners[position]
        holder.setData(bannerp, position)
    }

    inner class BannerViewHolder(private val binding: BannerListItemBinding) : RecyclerView.ViewHolder(binding.root){
        var currentBanner: BannerData? = null
        var currentPosition: Int = 0

        init {
            itemView.setOnClickListener {
                currentBanner?.let {
                    //context.showToast(currentLab!!.name + " Clicked !")
                }
            }

            //itemView.imgShare.setOnClickListener {

            //  currentLab?.let {
            //    val message: String = "My hobby is: " + currentLab!!.name

            //    val intent = Intent()
            //   intent.action = Intent.ACTION_SEND
            //   intent.putExtra(Intent.EXTRA_TEXT, message)
            //  intent.type = "text/plain"

            // context.startActivity(Intent.createChooser(intent, "Please select app: "))
            // }
            // }
        }

        fun setData(banner: BannerData?, pos: Int) {
            banner?.let {
                Picasso.with(context).load(banner.thumbnail).fit().centerCrop().into(binding.ivImage)

                binding.tvAbout.text = banner.description.toString()
                binding.tvTitle.text = banner.title.toString()
            }

            this.currentBanner = banner
            this.currentPosition = pos
        }

    }
    
}
