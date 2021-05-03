cmake_minimum_required(VERSION 3.5)

set(DobbyHome Dobby)
include_directories(${DobbyHome}/include)
add_subdirectory(${DobbyHome} dobby)
set(DOBBY_GENERATE_SHARED OFF CACHE INTERNAL "" FORCE)

set(NAME killer)
set(SOURCES dex_killer.cpp so_killer.cpp)
set(LIBRARIES log dobby)
add_library(${NAME} SHARED ${SOURCES})
target_link_libraries(${NAME} ${LIBRARIES})
