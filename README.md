OpenEmbedded/Yocto layer for Basler tools
=========================================

This layer contains SoC independent tools to operate Basler cameras,
namely `pylon`, [pypylon](https://github.com/basler/pypylon/) and
[gst-plugin-pylon](https://github.com/basler/gst-plugin-pylon).

To operate U3V and GigE cameras this is the only layer that is needed.

For the dart BCON for Mipi cameras an additional SoC specific support layer is
needed.
