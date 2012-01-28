package com.tort.mudai;


import com.tort.mudai.task.AbstractTask;

trait Handler {
    def handle(task: AbstractTask)
}
