package com.capstone.nik.mixology.di

import javax.inject.Qualifier

/**
 * The OkHttp client for images on other sites, such as a recipe post's photo.
 * It carries no reachability reporting: a site that is down says nothing about
 * whether Mixology's own server is.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ImageClient
