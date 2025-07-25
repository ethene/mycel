package com.quantumresearch.mycel.app.headless.blogs

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class HeadlessBlogModule {

    @Provides
    @Singleton
    internal fun provideBlogController(blogController: BlogControllerImpl): BlogController {
        return blogController
    }

}
